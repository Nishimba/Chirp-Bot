package com.nish;

import com.dbase.VODCommands;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        //TODO: ALL FOLLOWING COMMANDS MUST BE ADMIN ONLY

        //Stop command
        Command stopCommand = new Command("stop", "Shuts down Chirp.", null, false)
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
                //convert space separated list to commas
                args = BotUtils.convertArgsToList(args);

                //add the word to the file
                BotUtils.AppendStrToFile("res/" + args[1] + ".txt", args[2]);
                BotUtils.SendMessage(event.getChannel(), args[2] + " Added!");
            }
        };

        //addition of commands to hashmap
        commandMap.put(helpCommand.commandName, helpCommand);
        commandMap.put(heroCommand.commandName, heroCommand);
        commandMap.put(mapCommand.commandName, mapCommand);
        commandMap.put(stopCommand.commandName, stopCommand);
        commandMap.put(editFileCommand.commandName, editFileCommand);

        new VODCommands(commandMap);
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
