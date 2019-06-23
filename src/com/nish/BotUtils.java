package com.nish;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.api.internal.json.objects.GuildObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
/*
 * Created by Nishimba & Daalekz on 06/01/19
 * Basic Utilities for any class in the Bot to use
 */

public class BotUtils
{
    //Prefix for commands
    static String BOT_PREFIX = "!";

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
            return Files.readAllLines(Paths.get(filePath));//return a list of all lines in the file
        }
        catch (Exception e)
        {
            System.out.println("Error in ReadLines method.");
            e.printStackTrace();
            return null;
        }
    }

    //method for appending lines to a files
    static void AppendStrToFile(String filePath, String content)
    {
        try
        {
            //get the current contents of the file, and alphabetise it
            List<String> alphaList = ReadLines(filePath);
            alphaList.add(content);
            Collections.sort(alphaList);

            //create a writeable string from the alphabetised contents
            String tempString = "";
            for (int i = 0; i < alphaList.size(); i++)
            {
                tempString = tempString.concat(alphaList.get(i) + "\r\n");
            }

            Files.write(Paths.get(filePath), tempString.getBytes(), StandardOpenOption.WRITE);//overwrite file with new list
        }
        catch(IOException e)
        {
            System.out.println("Exception occurred in AppendStrToFile");
            e.printStackTrace();
        }
    }

    //returns true if the given string is found within the given file. otherwise returns false.
    static boolean SearchFile(String filePath, String content)
    {
       //return true if the file contains requested string
       List<String> fileList = ReadLines(filePath);
       return fileList.contains(content);
    }

    //Send embed to a given channel, with some exception catching
    static void SendEmbed(IChannel channel, EmbedObject embed)
    {
        //send message with error catching
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

    //string funnel for files
    static String StringFunnel(String filePath, String checkString)
    {
        return ListCompare(ReadLines(filePath), checkString);
    }

    //String funnel for commands
    static String StringFunnel(HashMap<String,Command> checkMap, String checkString)
    {
        List<String> keys = new ArrayList<>(checkMap.keySet());
        return ListCompare(keys, checkString);
    }

    //Method to compare all the entries in a list against a given string.
    private static String ListCompare(List<String> candidates, String checkString)
    {
        double threshold = 0.7;//this will only return if above threshold
        int index = 0;//the index in the string to check
        double max = 0;//the maximum found sim index
        String maxPairValue = "";//the value of the maximum sim

        //iterate through the potential matches
        while (index < candidates.size())
        {
            //check the similarity for the current match
            double similarityIndex = (StringSimilarity.similarity(candidates.get(index), checkString));

            //if the similarity of the new match is greater than any previous match
            if(similarityIndex > max)
            {
                //put the new information into variables
                max = similarityIndex;
                maxPairValue = candidates.get(index);
            }
            index++;
        }
        //uncomment this for debugging purposes ie. fine-tuning the threshold for suggestions.
        //return maxPairValue + ":" + max;

        //if max is above the threshold, return the match
        if(max > threshold)
        {
            return maxPairValue;
        }
        return null;
    }
    //A method that returns the list of all the guilds that the bot is a part of.
    public static List<IGuild> GetGuilds(IDiscordClient client)
    {
        return client.getGuilds();
    }

}


