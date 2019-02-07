package com.nish;


import org.apache.commons.io.*;
import org.json.JSONObject;

import java.io.*;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;
import java.io.IOException;
import java.io.InputStream;

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
    private static final String APIKEY = BotUtils.ReadLines("res/APIKEY.txt").get(0);


    //If youtube video id encoding changes, these will no longer function.
    private final String youTubeUrlRegEx = "^(https?)?(://)?(www.)?(m.)?((youtube.com)|(youtu.be))/";
    private final String[] videoIdRegex = { "\\?vi?=([^&]*)","watch\\?.*v=([^&]*)", "(?:embed|vi?)/([^/?]*)", "^([A-Za-z0-9\\-]*)"};

    public String extractVideoIdFromUrl(String url) {
        String youTubeLinkWithoutProtocolAndDomain = youTubeLinkWithoutProtocolAndDomain(url);

        for(String regex : videoIdRegex) {
            Pattern compiledPattern = Pattern.compile(regex);
            Matcher matcher = compiledPattern.matcher(youTubeLinkWithoutProtocolAndDomain);

            if(matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public Boolean checkLinkRegex(String url) {
        Pattern compiledPattern = Pattern.compile(youTubeUrlRegEx);
        Matcher matcher = compiledPattern.matcher(url);
        return matcher.find();
    }

    public String youTubeLinkWithoutProtocolAndDomain(String url) {
        Pattern compiledPattern = Pattern.compile(youTubeUrlRegEx);
        Matcher matcher = compiledPattern.matcher(url);

        if(matcher.find()){
            return url.replace(matcher.group(), "");
        }
        return url;
    }
    //pings the youtube api with the id of the video. converts the response to a string and determines if the video exists.
    public Boolean queryAPI(String input)
        {
            if(!checkLinkRegex(input))
            {
                return false;
            }
            String id = extractVideoIdFromUrl(input);

            String query = "https://www.googleapis.com/youtube/v3/videos?part=id&id="+id+"&key="+APIKEY;
            //To check response by hand ie if the api changes in future, uncomment the prinln, run the function, and click the link generated in console
            //System.out.println("query is: "+query);
           try
           {
               URLConnection connection = new URL(query).openConnection();
               InputStream response = new URL(query).openStream();
               String responseString = getStringFromInputStream(response);
               if(responseString.contains("\"totalResults\": 1,"))
               {
                   return true;
               }
               else if(responseString.contains("\"totalResults\": 0,"))
               {
                   return false;
               }
           }
           catch(IOException e)
           {
               System.out.println(e);
               return null;
           }
            return null;
        }

        //Thanks to Mykong for this function.
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

}
