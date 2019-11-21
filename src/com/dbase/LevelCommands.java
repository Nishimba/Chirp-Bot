package com.dbase;

import com.nish.BotUtils;
import com.nish.Command;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class LevelCommands
{
    public LevelCommands() { }

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
        Command rankCommand = new Command("rank", "Display the users current rank", new String[]{"~rank [@user]"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                if(args.length == 1)
                {
                    File output = LevelUtils.createRankCard(event.getAuthor(), event.getGuild());
                    BotUtils.SendFile(event.getChannel(), output);
                    return true;
                }
                else if(args.length == 2)
                {
                    if(LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()) != -1)
                    {
                        File output = LevelUtils.createRankCard(event.getMessage().getMentions().get(0), event.getGuild());
                        BotUtils.SendFile(event.getChannel(), output);
                        return true;
                    }
                }
                return false;
            }
        };

        Command xpCommand = new Command("modifyXP", "Add/remove an amount of XP to/from a user", new String[]{"~modifyxp amount @user"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    LevelUtils.addXP(Integer.parseInt(args[1]), event.getMessage().getMentions().get(0), event.getGuild());
                    LevelUtils.updateRoles(event.getMessage().getMentions().get(0), event.getGuild());
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getName() + "'s XP was modified to " + LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()));
                    return true;
                }
                catch (Exception e)
                {
                    return false;
                }
            }
        };

        Command levelCommand = new Command("modifyLevel", "Add/remove an amount of Levels to/from a user", new String[]{"~modifyLevel amount @mention"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    int currentLevel = LevelUtils.getCurrentLevel(event.getMessage().getMentions().get(0), event.getGuild());
                    int targetLevel = currentLevel + Integer.parseInt(args[1]);

                    int xpRequired = LevelUtils.xpRequiredForLevel(targetLevel, event.getMessage().getMentions().get(0), event.getGuild());

                    LevelUtils.addXP(xpRequired, event.getMessage().getMentions().get(0), event.getGuild());
                    LevelUtils.updateRoles(event.getMessage().getMentions().get(0), event.getGuild());
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getName() + "'s level was modified to " + LevelUtils.getCurrentLevel(event.getMessage().getMentions().get(0), event.getGuild()));
                    return true;
                }
                catch (Exception e)
                {
                    return false;
                }
            }
        };

        Command setXPCommand = new Command("setXP", "Set users XP", new String[]{"~setXP amount @mention"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    int currentXP = LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild());
                    int targetXP = Integer.parseInt(args[1]);
                    int diff = targetXP - currentXP;
                    LevelUtils.addXP(diff, event.getMessage().getMentions().get(0), event.getGuild());
                    LevelUtils.updateRoles(event.getMessage().getMentions().get(0), event.getGuild());
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getName() + "'s XP was set to " + LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()));
                    return true;
                }
                catch (Exception e)
                {
                    return false;
                }
            }
        };

        Command setLevelCommand = new Command("setLevel", "Set users level", new String[]{"~setLevel amount @mention"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    int xpRequired = LevelUtils.xpRequiredForLevel(Integer.parseInt(args[1]), event.getMessage().getMentions().get(0), event.getGuild());

                    LevelUtils.addXP(xpRequired, event.getMessage().getMentions().get(0), event.getGuild());
                    LevelUtils.updateRoles(event.getMessage().getMentions().get(0), event.getGuild());
                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getMentions().get(0).getName() + "'s level was set to " + LevelUtils.getCurrentLevel(event.getMessage().getMentions().get(0), event.getGuild()));
                    return true;
                }
                catch (Exception e)
                {
                    return false;
                }
            }
        };

        Command changeMultiplierCommand = new Command("changeMultiplier", "changes multiplier for a role", new String[]{"~changeMultiplier multiplier @role"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                try {
                    if (args.length == 3)
                    {
                        LevelUtils.addMultiplier(event.getGuild(), event.getMessage().getRoleMentions().get(0), Double.parseDouble(args[1]), false);
                    }
                    else
                    {
                        LevelUtils.addMultiplier(event.getGuild(), event.getMessage().getRoleMentions().get(0), Double.parseDouble(args[1]), true);
                    }

                    BotUtils.SendMessage(event.getChannel(), event.getMessage().getRoleMentions().get(0).getName() + "'s multiplier was set to " + LevelUtils.getMultiplier(event.getGuild(), event.getMessage().getRoleMentions().get(0)));
                    return true;
                }
                catch (Exception e)
                {
                    return false;
                }
            }
        };

        Command addLevelCutoffCommand = new Command("addLevelRole", "Adds a role to be given to users with the specified level", new String[]{"~addLevelRole @role level"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                IRole role = event.getMessage().getRoleMentions().get(0);
                int level = Integer.parseInt(args[2]);
                LevelUtils.addRoleToDB(role, event.getGuild(), level);
                BotUtils.SendMessage(event.getChannel(), "Role " + role.getName() + " was given a level requirement of " + level);
                return true;
            }
        };

        Command top10Command = new Command("leaderboard", "Outputs the top 10 users on the server", new String[]{"~leaderboard"}, false)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                ResultSet results = LevelUtils.topN(event.getGuild(), 10);

                int i = 0;

                try
                {
                    if (results != null)
                    {
                        while (results.next())
                        {
                            i++;
                        }
                    }
                }
                catch (SQLException e)
                {
                    return false;
                }

                BotUtils.SendFile(event.getChannel(), LevelUtils.createLeaderboardCard(event, event.getGuild(), i));
                return true;
            }
        };

        Command motmRoll = new Command("motmroll", "Rolls for a new member of the month based on the top 25 users in the leaderboard.", new String[]{"~motmroll"}, false)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                //Generate top 25 Users and store them.
                ResultSet top25Users = LevelUtils.topN(event.getGuild(), 25);

                int i = 0;

                try
                {
                    if (top25Users != null)
                    {
                        while(top25Users.next())
                        {
                            i++;
                        }

                        top25Users.beforeFirst(); //Put the pointer of the set to before the first index
                        //i is now equal to the amount of users in the top 25 list (just in-case there isn't 25 people in the leaderboards bc dead server lol)
                        //Time to pick a WINNER!
                        double rankIndex = LevelUtils.getRandomIntegerBetweenRange(1, i);
                        for(int j = 0; j < rankIndex; j++)
                        {
                            top25Users.next();
                        }

                        IUser winningUser = event.getGuild().getUserByID(top25Users.getLong(1)); //Gets the user from the randomly generated winning index
                        //winningUser.addRole(LevelUtils.selectMOTMRole(event.getGuild())); //TODO: Add this back later.

                        BotUtils.SendMessage(event.getChannel(), winningUser.mention() + " was drawn as the winner of MOTM!");
                        return true;
                    }
                    return false;
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                    return false;
                }
            }
        };

        Command viewCurrentMultipliers = new Command("multipliers", "Shows your current XP modifiers.", new String[]{"~multipliers [@user]"}, false)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                if(args.length == 1)
                {
                    BotUtils.SendMessage(event.getChannel(), "This command was sent without arguments.");
                    //View current multipliers of current user
                    LevelUtils.PrintMultipliers(event.getGuild(), event.getAuthor(), event.getChannel());
                    return true;
                }
                else if(args.length == 2)
                {
                    //View current multipliers of a given user
                    LevelUtils.PrintMultipliers(event.getGuild(), event.getMessage().getMentions().get(0), event.getChannel());
                    return true;
                }
                else
                {
                    return false;
                }
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
        commandMap.put(motmRoll.commandName, motmRoll);
        commandMap.put(viewCurrentMultipliers.commandName, viewCurrentMultipliers);
    }
}
