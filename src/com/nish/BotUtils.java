package com.nish;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;


/*
 * Created by Nishimba & Daalekz on 06/01/19
 * Basic Utilities for any class in the Bot to use
 */

public class BotUtils
{
    //Prefix for commands
    public static String BOT_PREFIX = "~";

    //Create the client to connect to the server
    static IDiscordClient getBuiltDiscordClient(String token)
    {
        return new ClientBuilder()
                .withToken(token)
                .build();
    }

    //Send message to a given channel, with some exception catching
    public static void SendMessage(IChannel channel, String message)
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

    //Send file to a given channel, with some exception catching
    public static void SendFile(IChannel channel, File image)
    {
        RequestBuffer.request(() -> {
            try
            {
                channel.sendFile(image);
            }
            catch (Exception e)
            {
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
            }
        });
    }

    //Read lines from a given file(the file path is given as an argument) and output a list of each line of the file.
    public static List<String> ReadLines(String filePath)
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
            if (alphaList != null)
            {
                alphaList.add(content);

                Collections.sort(alphaList);

                //create a writable string from the alphabetised contents
                String tempString = "";
                for (String s : alphaList)
                {
                    tempString = tempString.concat(s + "\r\n");
                }

                Files.write(Paths.get(filePath), tempString.getBytes(), StandardOpenOption.WRITE);//overwrite file with new list
            }
            else
            {
                System.out.println("Error writing to file.");
            }
        }
        catch(IOException e)
        {
            System.out.println("Exception occurred in AppendStrToFile");
            e.printStackTrace();
        }
    }

    //Send embed to a given channel, with some exception catching
    public static void SendEmbed(IChannel channel, EmbedObject embed)
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

    //print the usages for commands
    public static String OutputUsage(String CommandNameString, HashMap<String, Command> checkMap)
    {
        //append a new line followed by each usage
        StringBuilder builtString = new StringBuilder();
        for (String usage: checkMap.get(CommandNameString).usages)
        {
            builtString.append("\r\n").append(usage);
        }

        return builtString.toString();
    }

    //String funnel for commands
    static String StringFunnel(HashMap<String,Command> checkMap, String checkString)
    {
        List<String> keys = new ArrayList<>(checkMap.keySet());
        return ListCompare(keys, checkString, 0.85);
    }

    //Method to compare all the entries in a list against a given string.
    public static String ListCompare(List<String> candidates, String checkString, double close)
    {
        int index = 0;//the index in the string to check
        double max = 0;//the maximum found sim index
        String maxPairValue = "";//the value of the maximum sim

        //iterate through the potential matches
        while (index < candidates.size())
        {
            //check the similarity for the current match
            double similarityIndex = StringSimilarity.similarity(candidates.get(index), checkString);

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
        if(max > close)
        {
            return maxPairValue;
        }
        return null;
    }

    public static String[] convertArgsToList(String[] args)
    {
        //convert the array to string list
        ArrayList<String> tempList = new ArrayList<>(Arrays.asList(args));
        tempList.remove(0);
        StringBuilder builder = new StringBuilder();
        for(String s : tempList)
        {
            builder.append(s);
        }
        String temp = builder.toString();

        return temp.split(",");
    }

    //A method that returns the list of all the guilds that the bot is a part of.
    static List<IGuild> GetGuilds(IDiscordClient client)
    {
        return client.getGuilds();
    }

    //prints all guilds the bot is in
    static void PrintGuilds(IDiscordClient client)
    {
        for(IGuild guild : client.getGuilds())
        {
            System.out.println(guild.getName());
        }
    }
}
