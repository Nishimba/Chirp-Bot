package com.nish;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by Nishimba on 06/01/19
 * Basic Utilities for any class in the Bot to use
 */

public class BotUtils
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
    //IO handling
    //read from any file
    //write to any file
    public List<String> ReadLines(String filePath)//add a switch for delimiter?
    {

        BufferedReader lineReader = new BufferedReader(new FileReader(filePath));
        List<String> lines = new ArrayList<String>();

        while((String line = lineReader.readLine()) != null)
        {
            lines.add(line);
            return lines;
        }
    }
    //**************************************
}
