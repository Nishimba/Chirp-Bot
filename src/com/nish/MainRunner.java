package com.nish;

import sx.blah.discord.api.IDiscordClient;

import java.io.*;

/*
 * Created by Nishimba on 06/01/19
 */

public class MainRunner
{
    //What to do when the bot starts
    public static void main(String[] args)
    {
        //build the client
        IDiscordClient cli = BotUtils.getBuiltDiscordClient(BotUtils.ReadLines("res/BotToken.txt").get(0));

        //register it to listen to events in the MyEvents class
        cli.getDispatcher().registerListener(new CommandHandler());

        //login the client
        cli.login();
    }
}
