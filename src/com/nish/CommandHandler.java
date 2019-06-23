package com.nish;

import com.dbase.LevelCommands;
import com.dbase.VODCommands;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageHistory;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                BotUtils.SendMessage(event.getChannel(), "Tweet Tweet");
            }
        };

        //The help command
        Command helpCommand = new Command("help", "A help command showing how to use Chirp.", new String[] {"usage dbug"},true)
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
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                System.exit(0);
            }
        };

        //random args command
        Command argsCommand = new Command("args", "Argument commands", new String[] {"usage dbug"},true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                BotUtils.SendMessage(event.getChannel(), "Successful args");
            }
        };

        //Command to check youtube links.
        Command ytLinkCommand = new Command("yt","Verify youtube links", null, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
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

        //list all the heroes command
        //this will be used to test all the different fileio operations in the near future.
        Command heroCommand = new Command("heroes", "List all the heroes in Overwatch!", new String[] {"list add search check"},true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
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
                for(int i = 1; i < args.length; i++)
                {
                    stringToMatch += args[i];
                    stringToMatch += " ";
                }
                stringToMatch = stringToMatch.trim();

                List<IMessage> history = new ArrayList<>();
                for(IChannel chan : event.getGuild().getChannels())
                {
                    for (IMessage msg : chan.getFullMessageHistory().asArray())
                    {
                        history.add(msg);
                    }
                }

                Map<IUser, Integer> count = new HashMap<>();

                for(IMessage m : history)
                {
                    if(m.getContent().toLowerCase().contains(stringToMatch.toLowerCase()))
                    {
                        if(count.putIfAbsent(m.getAuthor(), 1) != null)
                        {
                            count.replace(m.getAuthor(), count.get(m.getAuthor()) + 1);
                        }
                    }
                }

                Map.Entry<IUser, Integer> highestCount = null;
                for(Map.Entry<IUser, Integer> entry : count.entrySet())
                {
                    if(highestCount == null)
                    {
                        highestCount = entry;
                    }
                    else if(entry.getValue() > highestCount.getValue())
                    {
                        highestCount = entry;
                    }
                }
                message.edit(highestCount.getKey().getName() + " has said *" + stringToMatch + "* the most times, with " + highestCount.getValue() + " occurrences");
            }
        };

        //addition of commands to hashmap
        commandMap.put(testCommand.commandName, testCommand);
        commandMap.put(helpCommand.commandName, helpCommand);
        commandMap.put(argsCommand.commandName, argsCommand);
        commandMap.put(stopCommand.commandName, stopCommand);
        commandMap.put(heroCommand.commandName, heroCommand);
        commandMap.put(ytLinkCommand.commandName, ytLinkCommand);
        commandMap.put(markdownCommand.commandName, markdownCommand);
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

            //if the command starts with the prefix and it is a command
            if(messageContent.startsWith(BotUtils.BOT_PREFIX) && commandMap.containsKey(commandArgs[0].substring(1)))
            {
                //get the command to be executed
                Command toExecute = commandMap.get(commandArgs[0].substring(1));

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
