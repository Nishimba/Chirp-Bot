package com.nish;

import com.dbase.DatabaseSetup;
import com.dbase.LevelCommands;
import com.dbase.LevelUtils;
import sx.blah.discord.api.IDiscordClient;

import java.util.Objects;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

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
        cli.getDispatcher().registerListener(new LevelCommands());
        cli.getDispatcher().registerListener(new CommandHandler());

        //login the client
        cli.login();

        //wait for the client to be logged in before polling what guilds the bot is in
        while(!cli.isLoggedIn())
        {
            try
            {
                Thread.sleep(100); //if the list is being output as blank, increase this number.
            }
            catch (java.lang.InterruptedException e)
            {
                System.out.println(e);
            }
        }

        //print the guilds the bot is in
        BotUtils.PrintGuilds(cli);

        // okay listen here cunt the fucking constructor for my class never runs so i cant put this there so its gotta be here
        LevelUtils.PopulateLevelBarriers();

        //setup the database info
        new DatabaseSetup(BotUtils.GetGuilds(cli));

        //Set Playing message
        cli.changePresence(StatusType.ONLINE, ActivityType.PLAYING, "Type ~help for help!");
    }
}
