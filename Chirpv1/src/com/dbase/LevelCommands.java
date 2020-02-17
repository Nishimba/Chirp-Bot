package com.dbase;

import com.nish.BotUtils;
import com.nish.Command;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
        Command rankCommand = new Command("rank", "Display the users current rank", new String[]{"~rank [@user]"}, false)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                if(args.length == 1)
                {
                    IMessage message = event.getChannel().sendMessage("Our code monkeys are compiling your profile picture. Hang on a sec...");
                    File output = LevelUtils.createRankCard(event.getAuthor(), event.getGuild());
                    message.delete();
                    BotUtils.SendFile(event.getChannel(), output);
                    return true;
                }
                else if(args.length == 2)
                {
                    if(LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild()) != -1)
                    {
                        IMessage message = event.getChannel().sendMessage("Our code monkeys are compiling your profile picture. Hang on a sec...");
                        File output = LevelUtils.createRankCard(event.getMessage().getMentions().get(0), event.getGuild());
                        message.delete();
                        BotUtils.SendFile(event.getChannel(), output);
                        return true;
                    }
                }
                return false;
            }
        };

        Command xpCommand = new Command("modifyXP", "Add/remove an amount of XP to/from a user", new String[]{"~modifyxp <@user> <amount>"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    LevelUtils.addXP(Integer.parseInt(args[2]), event.getMessage().getMentions().get(0), event.getGuild());
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

        Command levelCommand = new Command("modifyLevel", "Add/remove an amount of Levels to/from a user", new String[]{"~modifylevel <@user> <amount>"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    int currentLevel = LevelUtils.getCurrentLevel(event.getMessage().getMentions().get(0), event.getGuild());
                    int targetLevel = currentLevel + Integer.parseInt(args[2]);

                    double xpRequired = LevelUtils.xpRequiredForLevel(targetLevel, event.getMessage().getMentions().get(0), event.getGuild());

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

        Command setXPCommand = new Command("setXP", "Set users XP", new String[]{"~setXP <@user> <amount>"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    double currentXP = LevelUtils.getCurrentXP(event.getMessage().getMentions().get(0), event.getGuild());
                    double targetXP = Double.parseDouble(args[2]);
                    double diff = targetXP - currentXP;
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

        Command setLevelCommand = new Command("setLevel", "Set users level", new String[]{"~setLevel <@user> <amount>"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    double xpRequired = LevelUtils.xpRequiredForLevel(Integer.parseInt(args[2]), event.getMessage().getMentions().get(0), event.getGuild());

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

        Command changeMultiplierCommand = new Command("changeMultiplier", "changes multiplier for a role", new String[]{"~changemultiplier <@role> <multiplier>"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                if(args.length == 3)
                {
                    try {
                        LevelUtils.addMultiplier(event.getGuild(), event.getMessage().getRoleMentions().get(0), Double.parseDouble(args[2]));
                        BotUtils.SendMessage(event.getChannel(), event.getMessage().getRoleMentions().get(0).getName() + "'s multiplier was set to " + LevelUtils.getMultiplierForRole(event.getGuild(), event.getMessage().getRoleMentions().get(0)));
                    }
                    catch (Exception e)
                    {
                        // TODO catch different exceptions and return different errors
                        BotUtils.SendMessage(event.getChannel(), "Arguments entered incorrectly. Role must be a mention of the role to add, and a multiplier must be provided as a double.");
                        return false;
                    }
                }
                else
                {
                    BotUtils.SendMessage(event.getChannel(), "Too few arguments entered!");
                    return false;
                }

                return true;
            }
        };

        Command addLevelCutoffCommand = new Command("addLevelRole", "Adds a role to be given to users with the specified level", new String[]{"~addlevelrole <@role> <level>"}, true)
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

        Command motmRoll = new Command("motmroll", "Rolls for a new member of the month based on the top 25 users in the leaderboard, and any users in your current voice chat if you're in one.", new String[]{"~motmroll"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                if(args.length != 1)
                {
                    return false;
                }

                //Generate top 25 Users and store them.
                ResultSet top25Users = LevelUtils.topN(event.getGuild(), 25);

                int i = 0;

                try
                {
                    if (top25Users != null)
                    {
                        ArrayList<Long> possibleUsers = new ArrayList<Long>();

                        //Find out how many users there are in the resultSet.
                        while(top25Users.next())
                        {
                            i++;
                        }
                        top25Users.beforeFirst(); //Put the pointer of the set to before the first index

                        //Add all users in the top25 to the ArrayList.
                        for(int j = 0; j < i; j++)
                        {
                            possibleUsers.add(top25Users.getLong(j));
                        }

                        //If the user is in the voice chat, add them to the list of entrants too
                        IVoiceChannel currentVC = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel();
                        if(currentVC != null)
                        {
                            //Return list of IUsers of all users connected to channel
                            ArrayList<IUser> connectedUsers = (ArrayList<IUser>) currentVC.getConnectedUsers();

                            for (IUser u: connectedUsers)
                            {
                                if(!u.getVoiceStateForGuild(event.getGuild()).isMuted() && !u.getVoiceStateForGuild(event.getGuild()).isDeafened())
                                {
                                    possibleUsers.add(u.getLongID());
                                }
                            }
                        }

                        //Time to pick a WINNER!
                        int rankIndex = (int) LevelUtils.getRandomIntegerBetweenRange(1, possibleUsers.size()); //Pick a random index in the list of the winners
                        IUser winningUser = event.getGuild().getUserByID(possibleUsers.get(rankIndex)); //Gets the user from the randomly generated winning index
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

        Command toggleMotm = new Command("toggleMOTM", "Toggles a specific role as the MOTM role for the current server.", new String[]{"~toggleMOTM <@role>"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                if(args.length == 2)
                {
                    int commandStatus = LevelUtils.toggleMOTM(event.getGuild(), event.getMessage().getRoleMentions().get(0));

                    /*
                        Return codes/values for the command:
                        0 - MOTM role was successfully swapped
                        1 - User is trying to enter a role as MOTM when one already exists
                        2 - New role was set as MOTM
                    */
                    switch (commandStatus)
                    {
                        case 0:
                            BotUtils.SendMessage(event.getChannel(), "The role " + event.getMessage().getRoleMentions().get(0) + " was already assigned as MOTM and has now been un-assigned.");
                            break;
                        case 1:
                            BotUtils.SendMessage(event.getChannel(), "A different role is already assigned as MOTM. Please un-assign the other role to be able to set a new role as the MOTM role for the server.");
                            break;
                        case 2:
                            BotUtils.SendMessage(event.getChannel(), "The role " + event.getMessage().getRoleMentions().get(0) + " has now been made the MOTM role for this server.");
                            break;
                        default:
                            BotUtils.SendMessage(event.getChannel(), "Unspecified error occurred during MOTM toggling process.");
                            break;
                    }
                }
                else
                {
                    BotUtils.SendMessage(event.getChannel(), "Please only use one argument!");
                    return false;
                }

                return true;
            }
        };

        Command awardXPForVoiceMembers = new Command("awardXPForVoicechatUsers", "Gives a set amount of XP to all users in the command executor's voice channel.", new String[]{"~awardXPForVoicechatUsers <amount>"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                if(args.length == 2)
                {
                    //Get the voice channel of the user
                    //Returns null if the user is not in a voice channel.
                    IVoiceChannel currentVC = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel();

                    //Check if currentVC returned null (are they in a voice channel)
                    if(currentVC != null)
                    {
                        //Return list of IUsers of all users connected to channel
                        ArrayList<IUser> connectedUsers = (ArrayList<IUser>) currentVC.getConnectedUsers();

                        //Loop through all connected users and grant the set amount of XP.
                        for(IUser u: connectedUsers)
                        {
                            if(!u.getVoiceStateForGuild(event.getGuild()).isMuted() && !u.getVoiceStateForGuild(event.getGuild()).isDeafened())
                            {
                                //Add the set amount of XP to each user.
                                LevelUtils.addXP(Double.parseDouble(args[1]), u, event.getGuild());
                            }
                        }

                        //Add message giving confirmation that XP has been added.
                        BotUtils.SendMessage(event.getChannel(), "Successfully added " + args[1] + "XP to " + connectedUsers.size() + " user(s).");
                    }
                    else
                    {
                        BotUtils.SendMessage(event.getChannel(), "You need to be in a voice channel for this command!");
                    }
                }
                else
                {
                    BotUtils.SendMessage(event.getChannel(), "Please only use one argument!");
                }

                return true;
            }
        };

        Command toggleXP = new Command("toggleXP", "toggles xp in the channel it is used in", new String[]{"~toggleXP"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                if(LevelUtils.toggleXPGainInChannel(event.getChannel()))
                {
                    BotUtils.SendMessage(event.getChannel(), "XP gains for this channel are now *ON*");
                }
                else
                {
                    BotUtils.SendMessage(event.getChannel(), "XP gains for this channel are now *OFF*");
                }
                return true;
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
        commandMap.put(toggleMotm.commandName, toggleMotm);
        commandMap.put(awardXPForVoiceMembers.commandName, awardXPForVoiceMembers);
        commandMap.put(toggleXP.commandName, toggleXP);
    }
}
