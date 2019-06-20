package com.dbase;

import com.nish.BotUtils;
import com.nish.Command;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class LevelCommands
{
    public LevelCommands()
    {

    }

    public LevelCommands(HashMap<String, Command> commandMap)
    {
        InitiateCommands(commandMap);
    }

    //Detect sent messages for XP allocation
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event)
    {
        LevelUtils.allocateXP(event);
    }

    private void InitiateCommands(HashMap<String, Command> commandMap)
    {
        Command arrayCommand = new Command("array", "Test for level barriers array.", new String[]{"~array"}, false)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                BotUtils.SendMessage(event.getChannel(), LevelUtils.PopulateLevelBarriers());
            }
        };

        Command rankCommand = new Command("rank", "Display the users current rank", new String[]{"~rank"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {

                if(args.length == 1)
                {
                    BotUtils.SendMessage(event.getChannel(), event.getAuthor().getDisplayName(event.getGuild()) + "'s XP: " + LevelUtils.getCurrentXP(event.getAuthor(), event.getGuild()));
                }
                else if(args.length == 2)
                {
                    if(LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()) != -1)
                    {
                        BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getDisplayName(event.getGuild()) + "'s XP: " + LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()));
                    }
                    else
                    {
                        BotUtils.SendMessage(event.getChannel(), "Bots can't get XP you moron!");
                    }
                }
            }
        };

        commandMap.put(arrayCommand.commandName, arrayCommand);
        commandMap.put(rankCommand.commandName, rankCommand);
    }
}
