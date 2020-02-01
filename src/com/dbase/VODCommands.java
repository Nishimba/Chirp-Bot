package com.dbase;

import com.nish.BotUtils;
import com.nish.Command;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

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
        Command addVodCommand = new Command("addvod", "Adds a VOD to the database.", new String[]{"~addvod [SR], [Hero], [Map], [YT Link]"}, false)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                ArrayList<Object> VODinfo = VODUtils.ValidateVODInfo(event, args);

                if(VODinfo != null)
                {
                    Integer VODID = VODUtils.AddVODRecord(VODinfo, event);
                    if(VODID != null)
                    {
                        event.getAuthor().getOrCreatePMChannel().sendMessage("Your VOD has the ID: " + VODID + ". Use this to delete or view the VOD.");
                    }
                    else
                    {
                        BotUtils.SendMessage(event.getChannel(), "There was an error retrieving VOD ID, please contact a staff member for help!");
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }
        };

        Command delVodCommand = new Command("deletevod", "Removes a VOD from the database.", new String[]{"~deletevod ID"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
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
                    return true;
                }
                catch (NumberFormatException e)
                {
                    return false;
                }
            }
        };

        Command getVodCommand = new Command("getvod", "Gets information for a VOD.", new String[]{"~getvod ID"}, false)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    VODUtils.SelectVODRecord(Integer.parseInt(args[1]), event);
                    return true;
                }
                catch (NumberFormatException e)
                {
                    return false;
                }
            }
        };

        commandMap.put(addVodCommand.commandName, addVodCommand);
        commandMap.put(delVodCommand.commandName, delVodCommand);
        commandMap.put(getVodCommand.commandName, getVodCommand);
    }
}
