package com.dbase;

import com.nish.BotUtils;
import com.nish.Command;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.io.File;
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
                    File output = LevelUtils.createRankCard(event.getAuthor(), event.getGuild());
                    BotUtils.SendFile(event.getChannel(), output);
                }
                else if(args.length == 2)
                {
                    if(LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()) != -1)
                    {
                        File output = LevelUtils.createRankCard(event.getMessage().getMentions().get(0), event.getGuild());
                        BotUtils.SendFile(event.getChannel(), output);
                    }
                    else
                    {
                        BotUtils.SendMessage(event.getChannel(), "Bots can't get XP you moron!");
                    }
                }
                else
                {
                    BotUtils.SendMessage(event.getChannel(), "Please only use one argument!");
                }
            }
        };

        Command xpCommand = new Command("modifyXP", "Add/remove an amount of XP to/from a user", new String[]{"~modifyXP amount @mention"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    LevelUtils.addXP(Integer.parseInt(args[1]), event.getMessage().getMentions().get(0), event.getGuild(), event);
                    LevelUtils.updateRoles(event.getMessage().getMentions().get(0), event.getGuild());
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getName() + "'s XP was modified to " + LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()));
                }
                catch (Exception e)
                {
                    BotUtils.SendMessage(event.getChannel(), "XP must be a number");
                }
            }
        };

        Command levelCommand = new Command("modifyLevel", "Add/remove an amount of Levels to/from a user", new String[]{"~modifyLevel amount @mention"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    int currentLevel = LevelUtils.getCurrentLevel(event.getMessage().getMentions().get(0), event.getGuild());
                    int targetLevel = currentLevel + Integer.parseInt(args[1]);

                    int xpRequired = LevelUtils.xpRequiredForLevel(targetLevel, event.getMessage().getMentions().get(0), event.getGuild());

                    LevelUtils.addXP(xpRequired, event.getMessage().getMentions().get(0), event.getGuild(), event);
                    LevelUtils.updateRoles(event.getMessage().getMentions().get(0), event.getGuild());
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getName() + "'s level was modified to " + LevelUtils.getCurrentLevel(event.getMessage().getMentions().get(0), event.getGuild()));
                }
                catch (Exception e)
                {
                    BotUtils.SendMessage(event.getChannel(), "Level must be a number");
                }
            }
        };

        Command setXPCommand = new Command("setXP", "Set users XP", new String[]{"~setXP amount @mention"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    int currentXP = LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild());
                    int targetXP = Integer.parseInt(args[1]);
                    int diff = targetXP - currentXP;
                    LevelUtils.addXP(diff, event.getMessage().getMentions().get(0), event.getGuild(), event);
                    LevelUtils.updateRoles(event.getMessage().getMentions().get(0), event.getGuild());
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getName() + "'s XP was set to " + LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()));
                }
                catch (Exception e)
                {
                    BotUtils.SendMessage(event.getChannel(), "XP must be a number");
                }
            }
        };

        Command setLevelCommand = new Command("setLevel", "Set users level", new String[]{"~setLevel amount @mention"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    int xpRequired = LevelUtils.xpRequiredForLevel(Integer.parseInt(args[1]), event.getMessage().getMentions().get(0), event.getGuild());

                    LevelUtils.addXP(xpRequired, event.getMessage().getMentions().get(0), event.getGuild(), event);
                    LevelUtils.updateRoles(event.getMessage().getMentions().get(0), event.getGuild());
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getName() + "'s level was set to " + LevelUtils.getCurrentLevel(event.getMessage().getMentions().get(0), event.getGuild()));
                }
                catch (Exception e)
                {
                    BotUtils.SendMessage(event.getChannel(), "Level must be a number");
                    e.printStackTrace();
                }
            }
        };

        Command changeMultiplierCommand = new Command("changeMultiplier", "changes multiplier for a role", new String[]{"~changeMultiplier multiplier @role"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try {
                    LevelUtils.changeMultiplier(event.getGuild(), event.getMessage().getRoleMentions().get(0), Double.parseDouble(args[1]));
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getRoleMentions().get(0).getName() + "'s multiplier was set to " + LevelUtils.getMultiplier(event.getGuild(), event.getMessage().getRoleMentions().get(0)));
                }
                catch (Exception e)
                {
                    BotUtils.SendMessage(event.getChannel(), "Modifier must be a number");
                }
            }
        };

        Command addLevelCutoff = new Command("addLevelRole", "Adds a role to be given to users with the specified level", new String[]{"~addLevelRole @role level"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                LevelUtils.addRoleToDB(event.getMessage().getRoleMentions().get(0), event.getGuild(), Integer.parseInt(args[2]));
            }
        };

        commandMap.put(rankCommand.commandName, rankCommand);
        commandMap.put(xpCommand.commandName, xpCommand);
        commandMap.put(levelCommand.commandName, levelCommand);
        commandMap.put(setXPCommand.commandName, setXPCommand);
        commandMap.put(setLevelCommand.commandName, setLevelCommand);
        commandMap.put(changeMultiplierCommand.commandName, changeMultiplierCommand);
        commandMap.put(addLevelCutoff.commandName, addLevelCutoff);
    }
}
