package com.nish;

import com.dbase.DatabaseSetup;
import sx.blah.discord.api.IDiscordClient;

import java.util.Objects;

/*
 * Created by Nishimba on 06/01/19
 */

public class MainRunner
{
    //What to do when the bot starts
    public static void main(String[] args)
    {
        //build the client
        IDiscordClient cli;
        try
        {
            cli = BotUtils.getBuiltDiscordClient(Objects.requireNonNull(BotUtils.ReadLines("res/BotToken.txt")).get(0));
        }
        catch(NullPointerException e)
        {
            cli = null;
            System.out.println("Bot Token was null and could not connect, shutting down.");
            System.exit(0);
        }

        //register it to listen to events in the MyEvents class
        cli.getDispatcher().registerListener(new CommandHandler());

        //login the client
        cli.login();

        //wait for the client to be logged in before polling what guilds the bot is in
        try
        {
            Thread.sleep(2000); //if the list is being output as blank, increase this number.
        }
        catch (java.lang.InterruptedException e)
        {
            e.printStackTrace();
        }

        //print the guilds the bot is in
        BotUtils.PrintGuilds(cli);

        //setup the database info
        new DatabaseSetup(BotUtils.GetGuilds(cli));
    }
}
