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

    //**************************************
    //**IO handling**
    //read from any file
    //write to any file
    //NOTE Command-query separation CQRS for large multiple user data manipulation sitations.


    //Read lines from a given file(the file path is given as an argument) and output a list of each line of the file.
    //Retrieve a specific entry with the .get() method and pass in the index of the entry you would like.

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
    //method for appending lines to a file.
    //TODO add sort function and then add that as a flaggable option to this function.
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
    public static boolean searchFile(String filePath, String content)
    {
       List<String> fileList = ReadLines(filePath);
       int index = 0;
       boolean found = false;
       while (index < fileList.size() -1)
       {
           if (fileList.get(index).equals(content))
           {
               found = true;
               break;
           }
           else
           {
               index ++;
           }
           //System.out.println(index + "   " + found);

       }

        return found;
    }
//    public static void AlphabeticallySortFile(String filePath)
//    {
//        try
//        {
//            //
//        }
//        catch(IOException e)
//        {
//            System.out.println("exception occured in sort" + e);
//        }
//    }

    //**************************************

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
    public static String StringFunnel(String filePath, String in)
{
    String input = StringUtils.capitalize(in);
    System.out.println(input + " is input.");
    Boolean found = searchFile(filePath,input);
    if (found)
    {
        return input;
    }
    else
    {
        //get a copy of the list of all the lines in the document.
        //run the similarity checker against each entry in the list
        //the string with the highest similarity is selected as a candidate. Possible "match gradients" implementation?- if several match closely then return all of them.
        List<String> fileList = ReadLines(filePath);
       // Map ranks = new HashMap<>();
        int index = 0;
        double max = 0;
        String maxPairValue = "";



        while (index < fileList.size() -1)
        {

            double similarityIndex = (StringSimilarity.similarity(fileList.get(index),input));
            //ranks.put(fileList.get(index), similarityIndex);
            if(similarityIndex > max)
            {
                max = similarityIndex;
                maxPairValue = fileList.get(index);
            }

            index++;

        }
//        System.out.println("Here is the entrySet from the ranks hashmap \n" + ranks.entrySet());
        return maxPairValue;

    }
}



}


