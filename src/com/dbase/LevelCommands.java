package com.dbase;

import com.nish.BotUtils;
import com.nish.Command;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        Command rankCommand = new Command("rank", "Display the users current rank", new String[]{"~rank"}, true, false)
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

        Command xpCommand = new Command("modifyXP", "Add/remove an amount of XP to/from a user", new String[]{"~modifyXP amount @mention"}, true, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    LevelUtils.addXP(Integer.parseInt(args[1]), event.getMessage().getMentions().get(0), event.getGuild());
                    LevelUtils.updateRoles(event.getMessage().getMentions().get(0), event.getGuild());
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getName() + "'s XP was modified to " + LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()));
                }
                catch (Exception e)
                {
                    // TODO catch different exceptions and return different errors
                    BotUtils.SendMessage(event.getChannel(), "XP must be a number");
                }
            }
        };

        Command levelCommand = new Command("modifyLevel", "Add/remove an amount of Levels to/from a user", new String[]{"~modifyLevel amount @mention"}, true, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    int currentLevel = LevelUtils.getCurrentLevel(event.getMessage().getMentions().get(0), event.getGuild());
                    int targetLevel = currentLevel + Integer.parseInt(args[1]);

                    int xpRequired = LevelUtils.xpRequiredForLevel(targetLevel, event.getMessage().getMentions().get(0), event.getGuild());

                    LevelUtils.addXP(xpRequired, event.getMessage().getMentions().get(0), event.getGuild());
                    LevelUtils.updateRoles(event.getMessage().getMentions().get(0), event.getGuild());
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getName() + "'s level was modified to " + LevelUtils.getCurrentLevel(event.getMessage().getMentions().get(0), event.getGuild()));
                }
                catch (Exception e)
                {
                    // TODO catch different exceptions and return different errors
                    BotUtils.SendMessage(event.getChannel(), "Level must be a number");
                }
            }
        };

        Command setXPCommand = new Command("setXP", "Set users XP", new String[]{"~setXP amount @mention"}, true, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    int currentXP = LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild());
                    int targetXP = Integer.parseInt(args[1]);
                    int diff = targetXP - currentXP;
                    LevelUtils.addXP(diff, event.getMessage().getMentions().get(0), event.getGuild());
                    LevelUtils.updateRoles(event.getMessage().getMentions().get(0), event.getGuild());
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getName() + "'s XP was set to " + LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()));
                }
                catch (Exception e)
                {
                    // TODO catch different exceptions and return different errors
                    BotUtils.SendMessage(event.getChannel(), "XP must be a number");
                }
            }
        };

        Command setLevelCommand = new Command("setLevel", "Set users level", new String[]{"~setLevel amount @mention"}, true, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    int xpRequired = LevelUtils.xpRequiredForLevel(Integer.parseInt(args[1]), event.getMessage().getMentions().get(0), event.getGuild());

                    LevelUtils.addXP(xpRequired, event.getMessage().getMentions().get(0), event.getGuild());
                    LevelUtils.updateRoles(event.getMessage().getMentions().get(0), event.getGuild());
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getName() + "'s level was set to " + LevelUtils.getCurrentLevel(event.getMessage().getMentions().get(0), event.getGuild()));
                }
                catch (Exception e)
                {
                    // TODO catch different exceptions and return different errors
                    BotUtils.SendMessage(event.getChannel(), "Level must be a number");
                }
            }
        };

        Command changeMultiplierCommand = new Command("changeMultiplier", "changes multiplier for a role", new String[]{"~changeMultiplier multiplier @role"}, true, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try {
                    LevelUtils.addMultiplier(event.getGuild(), event.getMessage().getRoleMentions().get(0), Double.parseDouble(args[1]));
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getRoleMentions().get(0).getName() + "'s multiplier was set to " + LevelUtils.getMultiplier(event.getGuild(), event.getMessage().getRoleMentions().get(0)));
                }
                catch (Exception e)
                {
                    // TODO catch different exceptions and return different errors
                    BotUtils.SendMessage(event.getChannel(), "Modifier must be a number");
                }
            }
        };

        Command addLevelCutoffCommand = new Command("addLevelRole", "Adds a role to be given to users with the specified level", new String[]{"~addLevelRole @role level"}, true, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                IRole role = event.getMessage().getRoleMentions().get(0);
                int level = Integer.parseInt(args[2]);
                LevelUtils.addRoleToDB(role, event.getGuild(), level);
                BotUtils.SendMessage(event.getChannel(), "Role " + role.getName() + " was given a level requirement of " + level);
            }
        };

        Command top10Command = new Command("leaderboard", "Outputs the top 10 users on the server", new String[]{"~leaderboard"}, false, false)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                ResultSet results = LevelUtils.topN(event.getGuild(), 10);

                int i = 0;

                try
                {
                    if (results != null)
                    {
                        while(results.next())
                        {
                            i++;
                        }
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }

                BotUtils.SendFile(event.getChannel(), LevelUtils.createLeaderboardCard(event, event.getGuild(), i));
            }
        };

        commandMap.put(rankCommand.commandName, rankCommand);
        commandMap.put(xpCommand.commandName, xpCommand);
        commandMap.put(levelCommand.commandName, levelCommand);
        commandMap.put(setXPCommand.commandName, setXPCommand);
        commandMap.put(setLevelCommand.commandName, setLevelCommand);
        commandMap.put(changeMultiplierCommand.commandName, changeMultiplierCommand);
        commandMap.put(addLevelCutoffCommand.commandName, addLevelCutoffCommand);
        commandMap.put(top10Command.commandName, top10Command);
    }
}
