package com.nish;

import org.jetbrains.annotations.NotNull;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.Arrays;
import java.util.HashMap;

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

    void OutputUsage()
    {
        //This needs to basically do what lines 73-61 do in a method to avoid code duplication
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

                builder.withAuthorName("Chirp Help");
                builder.withTitle("I'm here to help! This is everything I know.");
                System.out.println(args.length);
                if(args.length == 1) {
                    for (HashMap.Entry<String, Command> command: commandMap.entrySet())
                        {
                        builder.appendField(command.getValue().commandName, command.getValue().description, false);
                    }
                }
                //below here needs to be moved into OutputUsage
                else if (commandMap.containsKey(args[1]))
                {
                    StringBuilder buil = new StringBuilder();
                    builder.appendField(commandMap.get(args[1]).commandName, commandMap.get(args[1]).description, false);
                    for (String s: commandMap.get(args[1]).getUsage)
                    {
                        buil.append("\r\n" + s);
                    }
                    builder.appendField("Example uses of " + (commandMap.get(args[1]).commandName), buil.toString(), false);

                }

                //send the embed
                BotUtils.SendEmbed(event.getChannel(), builder.build());
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

        //addition of commands to hashmap
        commandMap.put(testCommand.commandName, testCommand);
        commandMap.put(helpCommand.commandName, helpCommand);
        commandMap.put(argsCommand.commandName, argsCommand);
    }

    //execute a command when the appropriate command is typed
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //store variables to easily access later
        String messageContent = event.getMessage().getContent();

        try
        {
            String[] commandArgs = messageContent.split(" ");
            for (String s: commandArgs) {
                System.out.println(s);
            }
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

                    builder.withTitle("You seemed to have used that command incorrectly! I'm here to help! This is everything I know about the " + toExecute.commandName + " command.");
                    builder.appendField(toExecute.commandName, toExecute.description, false);

                    //OutputUsage needs to be added here also

                    BotUtils.SendEmbed(event.getChannel(), builder.build());
                }
            }
        }
        catch (StringIndexOutOfBoundsException e)
        {
            System.out.println();
        }
    }
}
