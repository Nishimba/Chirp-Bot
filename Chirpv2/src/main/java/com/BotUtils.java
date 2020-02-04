package com;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BotUtils
{
    public static List<Guild> GetGuilds(DiscordClient cli)
    {
        return cli.getGuilds().collectList().block();
    }

    public static void SendMessage(MessageCreateEvent event, String message)
    {
        event.getMessage().getChannel().flatMap(reply->reply.createMessage(message)).subscribe();
    }

    //Read lines from a given file(the file path is given as an argument) and output a list of each line of the file.
    public static List<String> ReadLines(String filePath)
    {

        Logger logger = LoggerFactory.getLogger("File IO Logger");
        try
        {
            return Files.readAllLines(Paths.get(filePath));//return a list of all lines in the file
        }
        catch (Exception e)
        {
            logger.error("Error reading file + " + filePath);
            e.printStackTrace();
            return null;
        }
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
}
