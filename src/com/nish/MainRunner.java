package com.nish;

import com.dbase.DatabaseSetup;
import com.dbase.LevelCommands;
import com.dbase.LevelUtils;
import javafx.scene.media.MediaPlayer;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IGuild;
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
        IDiscordClient cli = BotUtils.getBuiltDiscordClient(BotUtils.ReadLines("res/BotToken.txt").get(0));

        //register it to listen to events in the MyEvents class
        cli.getDispatcher().registerListener(new LevelCommands());
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
            System.out.println(e);
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
