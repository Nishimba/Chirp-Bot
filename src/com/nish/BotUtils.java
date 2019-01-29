package com.nish;

import com.koloboke.collect.map.IntByteMap;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.*;
/*
 * Created by Nishimba on 06/01/19
 * Basic Utilities for any class in the Bot to use
 */

class BotUtils
{
    //Prefix for commands
    static String BOT_PREFIX = "~";

    //Create the client to connect to the server
    static IDiscordClient getBuiltDiscordClient(String token)
    {
        return new ClientBuilder()
                .withToken(token)
                .build();
    }

    //Send message to a given channel, with some exception catching
    static void SendMessage(IChannel channel, String message)
    {
        RequestBuffer.request(() -> {
            try
            {
                channel.sendMessage(message);
            }
            catch (DiscordException e)
            {
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
            }
        });
    }

    //Read lines from a given file(the file path is given as an argument) and output a list of each line of the file.
    public static List<String> ReadLines(String filePath) // todo add a switch for delimiter?
    {
        try
        {
            BufferedReader lineReader = new BufferedReader(new FileReader(filePath));
            List<String> lines = new ArrayList<>();
            String line = null;
            do
            {
                line = lineReader.readLine();
                lines.add(line);
            }
            while(line != null);
            return lines;
        }
        catch (Exception e)
        {
            System.out.println("Error in ReadLines method.");
            return null;
        }
    }

    //method for appending lines to a files
    public static void AppendStrToFile(String filePath, String content, Boolean appendMode)
    {
        try
        {
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(filePath, appendMode));
            String contentFormatted = content + "\n";
            out.write(contentFormatted);
            out.close();
        }
        catch(IOException e)
        {
            System.out.println("exception occurred in AppendStrToFile" + e);
        }
    }

    //returns true if the given string is found within the given file. otherwise returns false.
    public static boolean searchFile(String filePath, String content)
    {
       List<String> fileList = ReadLines(filePath);
       int index = 0;
       boolean found = false;
       while (index < fileList.size() -1) //iterate through the file till the end.
       {
           if (fileList.get(index).equals(content))
           {
               found = true;
               break; //
           }
           else
           {
               index ++;
           }
       }
        return found;
    }

    //Send embed to a given channel, with some exception catching
    static void SendEmbed(IChannel channel, EmbedObject embed)
    {
        RequestBuffer.request(() -> {
            try
            {
                channel.sendMessage(embed);
            }
            catch (DiscordException e)
            {
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
            }
        });
    }

    //Methods with overloads for String or hashmap to pipe into listcomparison
    public static List<String> StringFunnel(String filePath, String in)
    {
        String input = StringUtils.capitalize(in);
        List<String> fileList = ReadLines(filePath);
        return ListCompare(fileList, input);
    }
    public static List<String> StringFunnel(HashMap<String,Command> hashMap, String in)
    {
        List<String> keys = new ArrayList<>(hashMap.keySet());
        return ListCompare(keys,in);
    }

    //Method to compare all the entries in a list against a given string.
    public static List<String> ListCompare(List<String> candidates, String in)
    {
        String input = StringUtils.capitalize(in); //standardise all the strings.
        int index = 0;
        double max = 0;
        String maxPairValue = ""; //initialise
        double similarityIndex = 0;
        while (index < candidates.size())
            {
                similarityIndex = (StringSimilarity.similarity(candidates.get(index),input));
                if(similarityIndex > max)
                {
                    max = similarityIndex;
                    maxPairValue = candidates.get(index);
                }
                index++;
            }
        //uncomment this for debugging purposes ie. fine-tuning the threshold for suggestions.
        //return maxPairValue + ":" + max;
        List<String> results = new ArrayList<>(); //purpose of the list is to make it easier to expose what the stringsimilarity index is, when fine tuning threshold for suggestions.
        results.add(maxPairValue);
        results.add(Double.toString(max));
        return results;
    }
}


