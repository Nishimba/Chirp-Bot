package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by Daalekz on 07/02/19
 * Handles verifying youtube links.
 */

//Api query quota per day is 10k. Current quota status can be checked here.
//https://console.developers.google.com/apis/api/youtube.googleapis.com/quotas?project=chirpbot&duration=PT1H

//an adapted version jvanderwee's code for pattern matching provided youtube addresses and video id extraction.
// https://gist.github.com/jvanderwee/b30fdb496acff43aef8e
public class YTParser
{
    //Read in the youtube Api Key
    private static final String APIKEY = Objects.requireNonNull(BotUtils.ReadLines("res/APIKEY.txt")).get(0);

    //If youtube video id encoding changes, these will no longer function.
    //we don't actually have to pattern match the given url, but it is a faster preliminary step to filter out obviously bad urls.
    private final String youTubeUrlRegEx = "^(https?)?(://)?(www.)?(m.)?((youtube.com)|(youtu.be))/";
    private final String[] videoIdRegex = {"\\?vi?=([^&]*)", "watch\\?.*v=([^&]*)", "(?:embed|vi?)/([^/?]*)", "^([A-Za-z0-9\\-]*)"};

    //gets the video id from the given link.
    private String extractVideoIdFromUrl(String url)
    {
        String youTubeLinkWithoutProtocolAndDomain = youTubeLinkWithoutProtocolAndDomain(url);
        for (String regex : videoIdRegex)
        {
            Pattern compiledPattern = Pattern.compile(regex);
            Matcher matcher = compiledPattern.matcher(youTubeLinkWithoutProtocolAndDomain);
            if (matcher.find())
            {
                return matcher.group(1);
            }
        }
        return null;
    }

    //preliminary regex check to keep inputs clean. Avoids having to unnecessarily ping the api with invalid links.
    private Boolean checkLinkRegex(String url)
    {
        Pattern compiledPattern = Pattern.compile(youTubeUrlRegEx);
        Matcher matcher = compiledPattern.matcher(url);
        return matcher.find();
    }

    //sanitises link to allow each form of youtube link (share, etc.) to be processed.
    private String youTubeLinkWithoutProtocolAndDomain(String url)
    {
        Pattern compiledPattern = Pattern.compile(youTubeUrlRegEx);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find())
        {
            return url.replace(matcher.group(), "");
        }
        return url;
    }

    //pings the youtube api with the id of the video. converts the response to a string and determines if the video exists.
    public Boolean queryAPI(String input)
    {
        if (!checkLinkRegex(input))
        {
            return false;
        }
        String id = extractVideoIdFromUrl(input);

        //Query to hit the api with. it's essentially just a search. the "part" field determines what data is returned.
        //we only need the ID as we only need to know if the video exists. If in future we need more info,
        //like when it was uploaded or the length, we add that to this query. check the youtube documentation for more info.
        String query = "https://www.googleapis.com/youtube/v3/videos?part=id&id=" + id + "&key=" + APIKEY;

        //To check response by hand ie if the api changes in future, uncomment the prinln, run the function, and click the link generated in console
        System.out.println("query is: " + query);

        try
        {
            InputStream response = new URL(query).openStream();
            //convert the input stream, which is in JSON format, to a string.

            String responseString = getStringFromInputStream(response);
            //this phrase indicates if the video is found.
            if (responseString.contains("\"totalResults\": 1,"))
            {
                return true;
            }
            else if (responseString.contains("\"totalResults\": 0,"))
            {
                return false;
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    //Thanks to Mykong for this function.
    private static String getStringFromInputStream(InputStream is)
    {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try
        {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
