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
                    BotUtils.SendMessage(event.getChannel(), "Your VOD has been successfully added! The ID is: " + VODID +".");
                }
            }
        };

        commandMap.put(addVodCommand.commandName, addVodCommand);
    }
}
