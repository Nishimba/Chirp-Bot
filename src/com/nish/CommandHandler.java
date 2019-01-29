package com.nish;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.HashMap;
import java.util.List;

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
        for (String s: commandMap.get(CommandNameString).getUsage)
        {
            builtString.append("\r\n" + s);
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
                    if (commandMap.containsKey(args[1]))
                    {
                        builder.appendField(commandMap.get(args[1]).commandName, commandMap.get(args[1]).description, false);
                        if(commandMap.get(args[1]).getUsage != null)
                        {
                            builder.appendField("Example uses of " + (commandMap.get(args[1]).commandName), OutputUsage(args[1]), false);
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

        Command c9Command = new Command("c9", "c9c9c9c9", null,false)
        {
            void Execute(MessageReceivedEvent event, String[] args)
            {
                BotUtils.SendMessage(event.getChannel(), "LULULULUL Z9 C9 C4 V8 XPEKE PS4 AK47 F4 B12 CS1.6 007 36DD 3DS WD40 R34 N64 C3P0 R2D2 H2O XBOX1 Knight F1 to D2");
            }
        };
        Command modsCommand = new Command("mods", "mods BEST", null,false)
        {
            void Execute(MessageReceivedEvent event, String[] args)
            {
                BotUtils.SendMessage(event.getChannel(), "Nishimba is such a fucking good mod i swear to god it is amazing that this server has survived longer than any other don't get me started on how good of a mod Aliias is oh it boils my blood when other servers' mods don't do shit but this server if people don't want to be nice they're on the case (also btw i don't think i've seen other mods besides Rezha but he is lit chief if y'all out there keep it up and remember we need more mods like you)");
            }
        };

        //list all the heroes command
        //this will be used to test all the different fileio operations in the near future.
        Command heroCommand = new Command("heroes", "List all the heroes in Overwatch!", new String[] {"list add search check"},true)
    {
        void Execute(MessageReceivedEvent event, String[] args)
        {
//                System.out.println(args[1]+ "\n" + "This is the args 1");
            if (args[1].equals("list"))
            {
                //create an embed
                EmbedBuilder builder = new EmbedBuilder();
                builder.withAuthorName("Chirp's Hero Dictionary");
                List<String> tempList = BotUtils.ReadLines(args[2]); //TODO replace this. do it. please.
                Integer index = 0;
                String tempString = "";

                do {
                    tempString = tempString+(index+1)+". "+tempList.get(index)+"\n";
                    index ++;
                }
                while(index != tempList.size()-1);

                builder.appendField("Here's all the overwatch heroes i know!",tempString,false);
                BotUtils.SendEmbed(event.getChannel(), builder.build());
            }
            else if (args[1].equals("add"))
            {
//                 BotUtils.SendMessage(event.getChannel(), "Are you sure you want to add " + content + "?");
//                 //react to the message with tick or cross and then look for the first yes/no react as the bool trigger.
                System.out.println(args);
                String content = args[2];  //TODO replace with all trailing strings following the "add" argument. ie. "~heroes add Hero 29" would add everything after "add" ie. "Hero 29"
                //Current implementation only gets the following word.
                BotUtils.SendMessage(event.getChannel(), content +" Added!");
                BotUtils.AppendStrToFile("res/Heroes.txt", content, true);
            }
            else if (args[1].equals("search"))
            {
                String content = args[2];
                Boolean found = BotUtils.searchFile("res/Heroes.txt", content);
                if (found)
                {
                    BotUtils.SendMessage(event.getChannel(), content + " Found!");
                }
                else if (!found)
                {
                    BotUtils.SendMessage(event.getChannel(), content + " could not be found :frowning:");
                }
            }
            else if (args[1].equals("check"))
            {
                String content = args[2];
                String match = BotUtils.StringFunnel("res/Maps.txt", content);
                BotUtils.SendMessage(event.getChannel(), "Closest match I could find was:" + match);
            }
            else
            {
                BotUtils.SendMessage(event.getChannel(), "This doesn't seem to be a valid command. I can't give you help with this, sorry! :frowning: \n You gave me \""+ args[args.length -1] +"\"");
            }


        }
    };


        //addition of commands to hashmap
        commandMap.put(testCommand.commandName, testCommand);
        commandMap.put(helpCommand.commandName, helpCommand);
        commandMap.put(argsCommand.commandName, argsCommand);
        commandMap.put(stopCommand.commandName, stopCommand);
        commandMap.put(heroCommand.commandName, heroCommand);
        commandMap.put(c9Command.commandName, c9Command);
        commandMap.put(modsCommand.commandName, modsCommand);


    }

    //execute the command when the appropriate command is typed
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //store variables to easily access later
        String messageContent = event.getMessage().getContent();

        try
        {
            String[] commandArgs = messageContent.split(" ");
            //Uncomment this for debugging purposes:
            /*for (String s: commandArgs) {
                System.out.println(s);
            }*/

            if(messageContent.startsWith(BotUtils.BOT_PREFIX) && commandMap.containsKey(commandArgs[0].substring(1)))
            {
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

                //Recognising an invalid command
                //if the command isn't "Help", it definitely needs to be longer if it's supposed to take arguments.
                if(!toExecute.takesArgs && commandArgs.length > 1 || (toExecute.takesArgs && commandArgs.length == 1 && !toExecute.commandName.equals("help")))
                {
                    EmbedBuilder builder = new EmbedBuilder();

                    builder.withAuthorName("Chirp Help");

                    builder.withTitle("You seemed to have used the " + (toExecute.commandName) + " command incorrectly! I'm here to help - here's everything I know about " + toExecute.commandName + ":");
                    builder.appendField(toExecute.commandName, toExecute.description, false);
                    builder.appendField("Example uses of " + (toExecute.commandName), OutputUsage(toExecute.commandName), false);

                    //OutputUsage needs to be added here also

                    BotUtils.SendEmbed(event.getChannel(), builder.build());
                }
            }
            else if (messageContent.startsWith(BotUtils.BOT_PREFIX) && !commandArgs[0].substring(1).isEmpty())
            {
                String match = BotUtils.StringFunnel(commandMap, commandArgs[0].substring(1));
                if (match.isEmpty())
                {
                    System.out.println("Someone has serious spelling issues...");
                }
                else if (!match.isEmpty())
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
