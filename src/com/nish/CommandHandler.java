package com.nish;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by Nishimba on 08/01/19
 * Handles the creation and execution of commands
 */

public class CommandHandler
{
    //Hashmap to contain all commands indexed by their name
    private HashMap<String, Command> commandMap;

    //starts the command handler and initiates commands
    CommandHandler()
    {
        commandMap = new HashMap<>();
        InitiateCommands();
    }

    //print the usages for commands
    private String OutputUsage(String CommandNameString)
    {
        //append a new line followed by each usage
        StringBuilder builtString = new StringBuilder();
        for (String usage: commandMap.get(CommandNameString).usages)
        {
            builtString.append("\r\n" + usage);
        }

        return builtString.toString();
    }

    //when creating a command, put the execution here and add it to the hashmap
    private void InitiateCommands()
    {
        //simple test command
        Command testCommand = new Command("test", "A simple test command", new String[] {"~test (No extra arguments)", "example line 2", "example line 3"}, false)
        {
            void Execute(MessageReceivedEvent event, String[] args)
            {
                BotUtils.SendMessage(event.getChannel(), "Tweet Tweet");
            }
        };

        //The help command
        Command helpCommand = new Command("help", "A help command showing how to use Chirp.", new String[] {"usage dbug"},true)
        {
            void Execute(MessageReceivedEvent event, String[] args)
            {
                //Create an embed
                EmbedBuilder builder = new EmbedBuilder();

                //Basic blocks of the help embed that won't change depending on input
                builder.withAuthorName("Chirp Help");
                builder.withTitle("I'll try my best! Here's what I know.");

                //If just "~help" is given, the bot will return all commands it knows in the HashMap.
                if(args.length == 1)
                {
                    for (HashMap.Entry<String, Command> command : commandMap.entrySet()) {
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
                            builder.appendField("This is how you can use " + (commandMap.get(args[1]).commandName) + ":", OutputUsage(args[1]), false);
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

        //Stop command
        Command stopCommand = new Command("stop", "Shuts down Chirp.", null, false)
        {
            void Execute(MessageReceivedEvent event, String[] args)
            {
                System.exit(0);
            }
        };



        //random args command
        Command argsCommand = new Command("args", "Argument commands", new String[] {"usage dbug"},true)
        {
            void Execute(MessageReceivedEvent event, String[] args)
            {
                BotUtils.SendMessage(event.getChannel(), "Successful args");
            }
        };

        //Command to check youtube links.
        Command ytLinkCommand = new Command("yt","Verify youtube links", null, true)
        {
            void Execute(MessageReceivedEvent event, String[] args)
            {

                YTParser parser = new YTParser();
                Boolean valid = parser.queryAPI(args[1]);
                if(valid)
                {
                        BotUtils.SendMessage(event.getChannel(), "This video is valid! :heart:");
                }
                else
                {
                        BotUtils.SendMessage(event.getChannel(), "This video is invalid :frowning:");
                }
            }
        };

        //test converting timezones
        Command timeCommand = new Command("time", "converts current system time to another using the provided timezone code", new String[] {"!time now PST/ECT/BET/AET/JST","!time 09:00 PST"}, true)
        {

            void Execute(MessageReceivedEvent event, String[] args)
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
                    builder.appendField("Example uses of " + (commandName), OutputUsage(commandName), false);//output usages for the command

                    BotUtils.SendEmbed(event.getChannel(), builder.build());
                }
            }
        };

        //list all the heroes command
        //this will be used to test all the different fileio operations in the near future.
        Command heroCommand = new Command("heroes", "List all the heroes in Overwatch!", new String[] {"list add search check"},true)
        {
            void Execute(MessageReceivedEvent event, String[] args)
            {
                //if the heroes list is to be listed
                switch (args[1])
                {
                    case "list":
                    {
                        EmbedBuilder builder = new EmbedBuilder();//create an embed builder
                        builder.withAuthorName("Chirp's Hero Dictionary");
                        List<String> fileList = BotUtils.ReadLines("res/Heroes.txt");//read lines from a given filepath
                        String embedString = "";//the string that will contain the list

                        //for each string in the list, add it to the embed with the number and a newline
                        for (int heroNumber = 0; heroNumber < fileList.size(); heroNumber++)
                        {
                            embedString = embedString.concat(heroNumber + 1 + ". " + fileList.get(heroNumber) + "\r\n");
                        }

                        builder.appendField("Here's all the Overwatch heroes I know!", embedString, false);//add the string to the embed

                        BotUtils.SendEmbed(event.getChannel(), builder.build());//build and send the embed

                        break;
                    }
                    case "add":
                    {
                        //make sure that the hero to add includes everything after "add"
                        String content = "";
                        for(int i = 2; i < args.length; i++)
                        {
                            content = content.concat(args[i] + " ");
                        }
                        content = content.trim();

                        //Current implementation only gets the following word.
                        BotUtils.SendMessage(event.getChannel(), content + " Added!");
                        BotUtils.AppendStrToFile("res/Heroes.txt", content);
                        break;
                    }
                    case "search":
                    {
                        //checks the file for a specified string
                        String content = args[2];
                        boolean found = BotUtils.SearchFile("res/Heroes.txt", content);
                        if (found) {
                            BotUtils.SendMessage(event.getChannel(), content + " Found!");
                        } else {
                            BotUtils.SendMessage(event.getChannel(), content + " could not be found :frowning:");
                        }
                        break;
                    }
                    case "check":
                    {
                        //performs the comparison check on a specific file with a specific string
                        String content = args[2];
                        String match = BotUtils.StringFunnel("res/Maps.txt", content);
                        BotUtils.SendMessage(event.getChannel(), "Closest match I could find was:" + match);
                        break;
                    }
                    default:
                        BotUtils.SendMessage(event.getChannel(), "This doesn't seem to be a valid command. I can't give you help with this, sorry! :frowning: \n You gave me \"" + args[args.length - 1] + "\"");
                        break;
                }
            }
        };

        //addition of commands to hashmap
        commandMap.put(testCommand.commandName, testCommand);
        commandMap.put(helpCommand.commandName, helpCommand);
        commandMap.put(argsCommand.commandName, argsCommand);
        commandMap.put(stopCommand.commandName, stopCommand);
        commandMap.put(heroCommand.commandName, heroCommand);
        commandMap.put(ytLinkCommand.commandName, ytLinkCommand);
        commandMap.put(timeCommand.commandName,timeCommand);

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

            //if the command starts with the prefix and it is a command
            if(messageContent.startsWith(BotUtils.BOT_PREFIX) && commandMap.containsKey(commandArgs[0].substring(1)))
            {
                //get the command to be executed
                Command toExecute = commandMap.get(commandArgs[0].substring(1));

                //this is now a valid command!
                if(toExecute.takesArgs && commandArgs.length == 1 && toExecute.commandName.equals("help"))
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
                if(!toExecute.takesArgs && commandArgs.length > 1 || (toExecute.takesArgs && commandArgs.length == 1 && !toExecute.commandName.equals("help")))
                {
                    EmbedBuilder builder = new EmbedBuilder();

                    builder.withAuthorName("Chirp Help");

                    //flavour text with the information about the command
                    builder.withTitle("You seemed to have used the " + (toExecute.commandName) + " command incorrectly! I'm here to help - here's everything I know about " + toExecute.commandName + ":");
                    builder.appendField(toExecute.commandName, toExecute.description, false);
                    builder.appendField("Example uses of " + (toExecute.commandName), OutputUsage(toExecute.commandName), false);//output usages for the command

                    BotUtils.SendEmbed(event.getChannel(), builder.build());
                }
            }
            else if (messageContent.startsWith(BotUtils.BOT_PREFIX) && !commandArgs[0].substring(1).isEmpty())//if the command has a prefix but isnt a registered command
            {
                //check if the provided command is close to any other command
                String match = BotUtils.StringFunnel(commandMap, commandArgs[0].substring(1));
                if (match == null)
                {
                    System.out.println("Sorry, I don't know how to do that! If you need help, try the ~help command!");
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
