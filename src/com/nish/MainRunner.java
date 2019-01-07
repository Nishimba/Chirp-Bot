package com.nish;

import sx.blah.discord.api.IDiscordClient;

import java.io.*;

/*
 * Created by Nishimba on 06/01/19
 */

public class MainRunner
{
    //What to do when the bot starts
    public static void main(String[] args) throws IOException
    {
        //build the client
        IDiscordClient cli = BotUtils.getBuiltDiscordClient(GetTokenFromFile("res/BotToken.txt"));

        //register it to listen to events in the MyEvents class
        cli.getDispatcher().registerListener(new MyEvents());

        //login the client
        cli.login();
    }

    //Get the bot token from the specified file
    private static String GetTokenFromFile(String tokenPath) throws IOException
    {
        BufferedReader tokenRead = new BufferedReader(new FileReader(tokenPath));
        return tokenRead.readLine();
    }
}
