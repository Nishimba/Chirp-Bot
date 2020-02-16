package com.nish;

import com.dbase.LevelCommands;
import com.dbase.VODCommands;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
 * Created by Nishimba on 08/01/19
 * Handles the creation and execution of commands
 */

class CommandHandler
{
    //Hashmap to contain all commands indexed by their name
    private HashMap<String, Command> commandMap;

    //starts the command handler and initiates commands
    CommandHandler()
    {
        commandMap = new HashMap<>();
        InitiateCommands();
    }

    //when creating a command, put the execution here and add it to the hashmap
    private void InitiateCommands()
    {
        //The help command
        Command helpCommand = new Command("help", "A help command showing how to use Chirp.", new String[] {"~help", "~help [command]"},false)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                //Create an embed
                EmbedBuilder builder = new EmbedBuilder();

                //Basic blocks of the help embed that won't change depending on input
                builder.withAuthorName("Chirp Help");
                builder.withTitle("I'll try my best! Here's what I know.");

                //If just "~help" is given, the bot will return all commands it knows in the HashMap.
                if(args.length == 1)
                {
                    //alphabetise help
                    Map<String, Command> map = new TreeMap<>(commandMap);

                    //if admin, put the admin commands in the help command
                    for (Map.Entry<String, Command> command : map.entrySet())
                    {
                        if(command.getValue().isAdmin && event.getAuthor().getPermissionsForGuild(event.getGuild()).contains(Permissions.ADMINISTRATOR))
                        {
                            builder.appendField(command.getValue().commandName, command.getValue().description, false);
                        }
                        else if (!command.getValue().isAdmin)
                        {
                            builder.appendField(command.getValue().commandName, command.getValue().description, false);
                        }
                    }

                    //send the embed
                    BotUtils.SendEmbed(event.getChannel(), builder.build());
                    return true;
                }
                //Otherwise, the bot will return the usage, title and description of the command given in the first argument after the help command itself.
                else if(args.length == 2)
                {
                    //if the command map has the command
                    if (commandMap.containsKey(args[1]))
                    {
                        //append to the embed the name of the command and the description
                        builder.appendField(commandMap.get(args[1]).commandName, commandMap.get(args[1]).description, false);

                        //if the usages are not null, write them to the embed
                        if(commandMap.get(args[1]).usages != null)
                        {
                            builder.appendField("This is how you can use " + (commandMap.get(args[1]).commandName) + ":", BotUtils.OutputUsage(args[1], commandMap), false);
                        }

                        //send the embed once done
                        BotUtils.SendEmbed(event.getChannel(), builder.build());
                        return true;
                    }
                    else
                    {
                        BotUtils.SendMessage(event.getChannel(), "Sorry, but I don't know the command " + args[1] + ".");
                        return true;
                    }
                }
                return false;
            }
        };

        //Lists the heroes
        Command heroCommand = new Command("heroes", "List all the heroes in Overwatch!", new String[] {"~heroes"},false)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                EmbedBuilder builder = new EmbedBuilder();//create an embed builder
                builder.withAuthorName("Chirp's Hero Dictionary");
                List<String> fileList = BotUtils.ReadLines("res/Heroes.txt");//read lines from a given filepath
                String embedString = "";//the string that will contain the list

                //for each string in the list, add it to the embed with the number and a newline
                if (fileList != null)
                {
                    for (int heroNumber = 0; heroNumber < fileList.size(); heroNumber++)
                    {
                        embedString = embedString.concat(heroNumber + 1 + ". " + fileList.get(heroNumber) + "\r\n");
                    }

                    builder.appendField("Here's all the Overwatch heroes I know!", embedString, false);//add the string to the embed

                    BotUtils.SendEmbed(event.getChannel(), builder.build());//build and send the embed
                    return true;
                }
                return false;
            }
        };

        //Lists the heroes
        Command mapCommand = new Command("maps", "List all the maps in Overwatch!", new String[] {"~maps"},false)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                EmbedBuilder builder = new EmbedBuilder();//create an embed builder
                builder.withAuthorName("Chirp's Map Dictionary");
                List<String> fileList = BotUtils.ReadLines("res/Maps.txt");//read lines from a given filepath
                String embedString = "";//the string that will contain the list

                //for each string in the list, add it to the embed with the number and a newline
                if (fileList != null)
                {
                    for (int mapNumber = 0; mapNumber < fileList.size(); mapNumber++)
                    {
                        embedString = embedString.concat(mapNumber + 1 + ". " + fileList.get(mapNumber) + "\r\n");
                    }

                    builder.appendField("Here's all the Overwatch maps I know!", embedString, false);//add the string to the embed

                    BotUtils.SendEmbed(event.getChannel(), builder.build());//build and send the embed
                    return true;
                }
                return false;
            }
        };

        //test converting timezones
        Command timeCommand = new Command("time", "converts current system time to another using the provided timezone code", new String[] {"!time now PST/ECT/BET/AET/JST","!time 09:00 PST"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    //just return the current time for the provided timezone
                    String timeToConvert = args[1].toLowerCase();
                    String timeZone = args[2].toUpperCase();
                    List<String> validZones = new ArrayList<>(ZoneId.SHORT_IDS.keySet());

                    if (validZones.contains(timeZone))
                    {
                        if (timeToConvert.equals("now"))
                        {
                            ZoneId sourceZoneId = ZoneId.systemDefault();
                            ZoneId destZoneId = ZoneId.of(timeZone, ZoneId.SHORT_IDS);
                            BotUtils.SendMessage(event.getChannel(), TimeUtils.convertTime(destZoneId, ZonedDateTime.now(sourceZoneId)));
                            return true;
                        }
                        else
                        {
                            //check if provided timestamp is valid
                            String pattern = "^([0-1][0-9]|2[0-3]|[0-9]):[0-5][0-9]";
                            Pattern r = Pattern.compile(pattern);
                            Matcher m = r.matcher(timeToConvert);
                            if (m.find()) //is valid
                            {
                                ZoneId destZoneId = ZoneId.of(timeZone, ZoneId.SHORT_IDS);
                                ZonedDateTime time = ZonedDateTime.now().withHour(Integer.parseInt(timeToConvert.substring(0, 2))).withMinute(Integer.parseInt(timeToConvert.substring(3)));
                                BotUtils.SendMessage(event.getChannel(), TimeUtils.convertTime(destZoneId, time));
                                return true;
                            }
                        }
                    }
                    return false;
                }
                catch(Exception e)
                {
                    return false;
                }
            }
        };

        Command markdownCommand = new Command("markdown", "List the default markdown options", new String[] {"~markdown"},false)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                //Create an embed
                EmbedBuilder builder = new EmbedBuilder();

                // ive never escaped so many escapes in my life smile
                builder.withAuthorName("Markdown");
                builder.withTitle("Here's all the markdown options that discord provides:");
                builder.appendField("*Italics*", "\\*italics\\* or \\_italics\\_", false);
                builder.appendField("**Bold**", "\\*\\*bold\\*\\*", false);
                builder.appendField("***Bold Italics***", "\\*\\*\\*bold italics\\*\\*\\*", false);
                builder.appendField("__Underline__", "\\_\\_underline\\_\\_", false);
                builder.appendField("__*Underline Italics*__", "\\_\\_\\*underline italics\\*\\_\\_", false);
                builder.appendField("__**Underline Bold**__", "\\_\\_\\*\\*underline bold\\*\\*\\_\\_", false);
                builder.appendField("__***Underline Bold Italics***__", "\\_\\_\\*\\*\\*underline bold italics\\*\\*\\*\\_\\_", false);
                builder.appendField("~~Strikethrough~~", "\\~\\~strikethrough\\~\\~", false);
                builder.appendField("||Spoiler||", "\\|\\|spoiler\\|\\|", false);

                builder.appendField("The official discord documentation on markdown can be found here:", "https://support.discordapp.com/hc/en-us/articles/210298617-Markdown-Text-101-Chat-Formatting-Bold-Italic-Underline-", false);

                BotUtils.SendEmbed(event.getChannel(), builder.build());
                return true;
            }
        };

        Command stopCommand = new Command("stop", "Exits the bot safely.", null, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                System.exit(0);
                return true;
            }
        };

        //edits a file that is passed in, used for the editing of
        Command editFileCommand = new Command("editFile", "Appends a text file with the string provided.", new String[]{"~editFile [fileName], [content]"}, true)
        {
            public boolean Execute(MessageReceivedEvent event, String[] args)
            {
                //add the word to the file
                BotUtils.AppendStrToFile("res/" + args[1], args[2]);
                BotUtils.SendMessage(event.getChannel(), args[2] + " Added!");
                return true;
            }
        };

        //addition of commands to hashmap
        commandMap.put(helpCommand.commandName, helpCommand);
        commandMap.put(heroCommand.commandName, heroCommand);
        commandMap.put(mapCommand.commandName, mapCommand);
        commandMap.put(markdownCommand.commandName, markdownCommand);

        commandMap.put(timeCommand.commandName,timeCommand);
        commandMap.put(stopCommand.commandName, stopCommand);
        commandMap.put(editFileCommand.commandName, editFileCommand);

        new VODCommands(commandMap);
        new LevelCommands(commandMap);
    }

    //execute the command when the appropriate command is typed
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //store variables to easily access later
        String messageContent = event.getMessage().getContent();

        try
        {
            //content of the message in an array
            String[] commandArgs = messageContent.split(" ");
            String commandName = commandArgs[0].substring(1);

            //if the command starts with the prefix and it is a command
            if(messageContent.startsWith(BotUtils.BOT_PREFIX) && commandMap.containsKey(commandName))
            {
                //get the command to be executed
                Command toExecute = commandMap.get(commandName);

                //this is now a valid command!
                if(toExecute.isAdmin && !event.getAuthor().getPermissionsForGuild(event.getGuild()).contains(Permissions.ADMINISTRATOR) && !isOneOfTheBoys(event.getAuthor()))
                {
                    BotUtils.SendMessage(event.getChannel(), "You do not have permission to do that!");
                }
                else
                {
                    boolean result = toExecute.Execute(event, commandArgs);

                    //if the command is a command, but it has been used incorrectly, provide help
                    if(!result)
                    {
                        EmbedBuilder builder = new EmbedBuilder();

                        builder.withAuthorName("Chirp Help");

                        //flavour text with the information about the command
                        builder.withTitle("You seemed to have used the " + (toExecute.commandName) + " command incorrectly! I'm here to help - here's everything I know about " + toExecute.commandName + ":");
                        builder.appendField(toExecute.commandName, toExecute.description, false);
                        builder.appendField("Example uses of " + (toExecute.commandName), BotUtils.OutputUsage(toExecute.commandName, commandMap), false);//output usages for the command

                        BotUtils.SendEmbed(event.getChannel(), builder.build());
                    }
                }
            }
            else if (messageContent.startsWith(BotUtils.BOT_PREFIX) && !commandArgs[0].substring(1).isEmpty())//if the command has a prefix but isnt a registered command
            {
                //Omit messages that have the bot prefix twice (since the default prefix is ~ and ~~ makes a strikethrough message
                if(!messageContent.startsWith(BotUtils.BOT_PREFIX + BotUtils.BOT_PREFIX))
                {
                    //check if the provided command is close to any other command
                    String match = BotUtils.StringFunnel(commandMap, commandArgs[0].substring(1));
                    if (match == null)
                    {
                        BotUtils.SendMessage(event.getChannel(), "Sorry, I didn't recognise that command! Please use ~help to see available commands!");
                    }
                    else
                    {
                        BotUtils.SendMessage(event.getChannel(), "I couldn't find that command! The closest I could find was ``" + match + "``");
                    }
                }
            }
        }
        catch (StringIndexOutOfBoundsException e)
        {
            System.out.println();
        }
    }

    boolean isOneOfTheBoys(IUser user)
    {
        String id = user.getStringID();
        if(id.equals("175218556026355712") || id.equals("196489854987665408") || id.equals("163908872673689600") || id.equals("190386551081926656"))
        {
            return true;
        }
        return false;
    }
}
