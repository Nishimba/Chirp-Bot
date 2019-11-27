package com.nish;

import com.dbase.LevelCommands;
import com.dbase.VODCommands;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
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
        Command helpCommand = new Command("help", "A help command showing how to use Chirp.", new String[] {"~help", "~help [command]"},true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
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
                    for (Map.Entry<String, Command> command : map.entrySet()) {
                        builder.appendField(command.getValue().commandName, command.getValue().description, false);
                    }

                    //send the embed
                    BotUtils.SendEmbed(event.getChannel(), builder.build());
                }
                //Otherwise, the bot will return the usage, title and description of the command given in the first argument after the help command itself.
                else
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
                    }
                    else
                    {
                        BotUtils.SendMessage(event.getChannel(), "This doesn't seem to be a valid command. I can't give you help with this, sorry! :frowning:");
                    }
                }
            }
        };

        //Lists the heroes
        Command heroCommand = new Command("heroes", "List all the heroes in Overwatch!", new String[] {"~heroes"},false)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
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
                }
                else
                {
                    BotUtils.SendMessage(event.getChannel(), "Sorry, but I couldn't find the list of heroes!");
                }

                builder.appendField("Here's all the Overwatch heroes I know!", embedString, false);//add the string to the embed

                BotUtils.SendEmbed(event.getChannel(), builder.build());//build and send the embed
            }
        };

        //Lists the heroes
        Command mapCommand = new Command("maps", "List all the maps in Overwatch!", new String[] {"~maps"},false)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
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
                }
                else
                {
                    BotUtils.SendMessage(event.getChannel(), "Sorry, but I couldn't find the list of maps!");
                }
            }
        };

        //test converting timezones
        Command timeCommand = new Command("time", "converts current system time to another using the provided timezone code", new String[] {"!time now PST/ECT/BET/AET/JST","!time 09:00 PST"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
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
                        } else
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
                            } else
                            {
                                BotUtils.SendMessage(event.getChannel(), "You provided an invalid time, I can't convert it! Make sure the time is 24 hour time in format HH:MM.");
                            }
                        }
                    } else
                    {
                        BotUtils.SendMessage(event.getChannel(), "You provided an invalid timezone, I can't find the time for that!");
                    }
                }
                catch(Exception e)
                {
                    EmbedBuilder builder = new EmbedBuilder();

                    builder.withAuthorName("Chirp Help");

                    //flavour text with the information about the command
                    builder.withTitle("You seemed to have used the time command incorrectly! I'm here to help - here's everything I know about time:");
                    builder.appendField(commandName, description, false);
                    builder.appendField("Example uses of " + (commandName), BotUtils.OutputUsage(commandName, commandMap), false);//output usages for the command

                    BotUtils.SendEmbed(event.getChannel(), builder.build());
                }
            }
        };

        Command markdownCommand = new Command("markdown", "List the default markdown options", new String[] {"~markdown"},false)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
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
            }
        };

        Command countCommand = new Command("count", "Counts", new String[] {"~count string"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                IMessage message = event.getChannel().sendMessage("Working on it!");
                String stringToMatch = "";
                for (int i = 1; i < args.length; i++)
                {
                    stringToMatch += args[i];
                    stringToMatch += " ";
                }
                stringToMatch = stringToMatch.trim();

                List<IMessage> history = new ArrayList<>();
                for (IChannel chan : event.getGuild().getChannels())
                {
                    for (IMessage msg : chan.getFullMessageHistory().asArray())
                    {
                        history.add(msg);
                    }
                }

                Map<IUser, Integer> count = new HashMap<>();

                for (IMessage m : history)
                {
                    if (m.getContent().toLowerCase().contains(stringToMatch.toLowerCase()))
                    {
                        if (count.putIfAbsent(m.getAuthor(), 1) != null)
                        {
                            count.replace(m.getAuthor(), count.get(m.getAuthor()) + 1);
                        }
                    }
                }

                Map.Entry<IUser, Integer> highestCount = null;
                for (Map.Entry<IUser, Integer> entry : count.entrySet())
                {
                    if (highestCount == null)
                    {
                        highestCount = entry;
                    } else if (entry.getValue() > highestCount.getValue())
                    {
                        highestCount = entry;
                    }
                }
                message.edit(highestCount.getKey().getName() + " has said *" + stringToMatch + "* the most times, with " + highestCount.getValue() + " occurrences");
            }
        };

        Command stopCommand = new Command("stop", "Exits the bot safely.", null, false)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                System.exit(0);
            }
        };

        //edits a file that is passed in, used for the editing of
        Command editFileCommand = new Command("editFile", "Appends a text file with the string provided.", new String[]{"~editFile [fileName], [content]"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                //add the word to the file
                BotUtils.AppendStrToFile("res/" + args[1] + ".txt", args[2]);
                BotUtils.SendMessage(event.getChannel(), args[2] + " Added!");
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
        commandMap.put(countCommand.commandName, countCommand);

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
                if(toExecute.takesArgs && commandArgs.length == 1 && (toExecute.commandName.equals("help") || toExecute.commandName.equals("rank")))
                {
                    toExecute.Execute(event, commandArgs);
                }
                if(toExecute.takesArgs && commandArgs.length > 1)
                {
                    toExecute.Execute(event, commandArgs);
                }
                if(!toExecute.takesArgs && commandArgs.length == 1)
                {
                    toExecute.Execute(event, null);
                }

                //if the command is a command, but it has been used incorrectly, provide help
                //if the command isn't "Help", it definitely needs to be longer if it's supposed to take arguments.
                if(!toExecute.takesArgs && commandArgs.length > 1 || (toExecute.takesArgs && commandArgs.length == 1 && !(toExecute.commandName.equals("help") || toExecute.commandName.equals("rank"))))
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
            else if (messageContent.startsWith(BotUtils.BOT_PREFIX) && !commandArgs[0].substring(1).isEmpty())//if the command has a prefix but isnt a registered command
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
        catch (StringIndexOutOfBoundsException e)
        {
            System.out.println();
        }
    }
}
