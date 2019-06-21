package com.dbase;

import com.nish.BotUtils;
import com.nish.Command;
import org.slf4j.event.Level;
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
        Command rankCommand = new Command("rank", "Display the users current rank", new String[]{"~rank"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {

                if(args.length == 1)
                {
                    BotUtils.SendMessage(event.getChannel(), event.getAuthor().getDisplayName(event.getGuild()) + "'s Total XP: " + LevelUtils.getCurrentXP(event.getAuthor(), event.getGuild()) + " Level: " + LevelUtils.calculateCurrentLevel(event.getAuthor(), event.getGuild()));
                }
                else if(args.length == 2)
                {
                    if(LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()) != -1)
                    {
                        BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getDisplayName(event.getGuild()) + "'s Total XP: " + LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()) + " Level: " + LevelUtils.calculateCurrentLevel(event.getMessage().getMentions().get(0), event.getGuild()));
                    }
                    else
                    {
                        BotUtils.SendMessage(event.getChannel(), "Bots can't get XP you moron!");
                    }
                }
            }
        };

        Command xpCommand = new Command("modifyXP", "Add/remove an amount of XP to/from a user", new String[]{"~modifyXP amount @mention"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                LevelUtils.addXP(Integer.parseInt(args[1]), event.getMessage().getMentions().get(0), event.getGuild());
            }
        };

        Command levelCommand = new Command("modifyLevel", "Add/remove an amount of Levels to/from a user", new String[]{"~modifyLevel amount @mention"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                int currentLevel = LevelUtils.getCurrentLevel(event.getMessage().getMentions().get(0), event.getGuild());
                int targetLevel = currentLevel + Integer.parseInt(args[1]);

                int xpRequired = LevelUtils.xpRequiredForLevel(targetLevel, event.getMessage().getMentions().get(0), event.getGuild());

                LevelUtils.addXP(xpRequired, event.getMessage().getMentions().get(0), event.getGuild());
            }
        };

        Command setXPCommand = new Command("setXP", "Set users XP", new String[]{"~setXP amount @mention"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                int currentXP = LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild());
                int targetXP = Integer.parseInt(args[1]);
                int diff = targetXP - currentXP;
                LevelUtils.addXP(diff, event.getMessage().getMentions().get(0), event.getGuild());
            }
        };

        Command setLevelCommand = new Command("setLevel", "Set users level", new String[]{"~setLevel amount @mention"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                int xpRequired = LevelUtils.xpRequiredForLevel(Integer.parseInt(args[1]), event.getMessage().getMentions().get(0), event.getGuild());

                LevelUtils.addXP(xpRequired, event.getMessage().getMentions().get(0), event.getGuild());
            }
        };

        Command changeMultiplierCommand = new Command("changeMultiplier", "changes multiplier for a role", new String[]{"~changeMultiplier multiplier @role"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                LevelUtils.changeMultiplier(event.getGuild(), event.getMessage().getRoleMentions().get(0), Integer.parseInt(args[1]));
            }
        };

        commandMap.put(rankCommand.commandName, rankCommand);
        commandMap.put(xpCommand.commandName, xpCommand);
        commandMap.put(levelCommand.commandName, levelCommand);
        commandMap.put(setXPCommand.commandName, setXPCommand);
        commandMap.put(setLevelCommand.commandName, setLevelCommand);
        commandMap.put(changeMultiplierCommand.commandName, changeMultiplierCommand);
    }
}
