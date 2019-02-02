package com.nish;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Look at https://developers.google.com/youtube/v3/docs/videos/list#usage
//Using an adapted version jvanderwee's code for pattern matching provided youtube addresses and video id extraction.
// https://gist.github.com/jvanderwee/b30fdb496acff43aef8e
public class YTParser
{

    final String youTubeUrlRegEx = "^(https?)?(://)?(www.)?(m.)?((youtube.com)|(youtu.be))/";
    final String[] videoIdRegex = { "\\?vi?=([^&]*)","watch\\?.*v=([^&]*)", "(?:embed|vi?)/([^/?]*)", "^([A-Za-z0-9\\-]*)"};

    public String extractVideoIdFromUrl(String url) {
        String youTubeLinkWithoutProtocolAndDomain = youTubeLinkWithoutProtocolAndDomain(url);

        for(String regex : videoIdRegex) {
            Pattern compiledPattern = Pattern.compile(regex);
            Matcher matcher = compiledPattern.matcher(youTubeLinkWithoutProtocolAndDomain);

            if(matcher.find()){
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
}
