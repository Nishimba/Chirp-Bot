package com.dbase;

import com.nish.BotUtils;
import com.nish.Command;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class VODCommands
{
    public VODCommands(HashMap<String, Command> commandMap)
    {
        InitiateCommands(commandMap);
    }

    private void InitiateCommands(HashMap<String, Command> commandMap)
    {
        Command addVodCommand = new Command("addvod", "Adds a VOD to the database.", new String[]{"~addvod SR, Hero, Map, YT Link"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                ArrayList<Object> VODinfo = VODUtils.ValidateVODInfo(event, args);

                if(VODinfo != null)
                {
                    int VODID = VODUtils.AddVODRecord(VODinfo, event);
                    event.getAuthor().getOrCreatePMChannel().sendMessage("Your VOD has the ID: " + VODID + ". Use this to delete or view the VOD.");
                }
                else
                {
                    BotUtils.SendMessage(event.getChannel(), "There appears to be an error in your command, ");
                }
            }
        };

        Command delVodCommand = new Command("deletevod", "Removes a VOD from the database.", new String[]{"~deletevod ID"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    int success = VODUtils.DeleteVODRecord(Integer.parseInt(args[1]), event);
                    if(success == 1)
                    {
                        BotUtils.SendMessage(event.getChannel(), "Successfully deleted VOD!");
                    }
                    else
                    {
                        BotUtils.SendMessage(event.getChannel(), "VOD could not be found.");
                    }
                }
                catch (NumberFormatException e)
                {
                    BotUtils.SendMessage(event.getChannel(), "VOD ID must be a number.");
                }
            }
        };

        commandMap.put(addVodCommand.commandName, addVodCommand);
        commandMap.put(delVodCommand.commandName, delVodCommand);
    }
}
