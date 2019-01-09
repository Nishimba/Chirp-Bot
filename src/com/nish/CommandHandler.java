package com.nish;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

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

    //when creating a command, put the execution here and add it to the hashmap
    private void InitiateCommands()
    {
        //simple test command
        Command testCommand = new Command("test", "A simple test command", false)
        {
            void Execute(MessageReceivedEvent event, String[] args)
            {
                BotUtils.SendMessage(event.getChannel(), "Tweet Tweet");
            }
        };

        //The help command
        Command helpCommand = new Command("help", "A help command showing how to use Chirp.", true)
        {
            void Execute(MessageReceivedEvent event, String[] args)
            {
                EmbedBuilder builder = new EmbedBuilder();

                builder.withAuthorName("Chirp Help");

                //output help command if help is only command
                if(args.length == 1)
                {
                    builder.withTitle("I'm here to help! This is everything I know.");

                    for (HashMap.Entry<String, Command> command: commandMap.entrySet())
                    {
                        builder.appendField(command.getValue().commandName, command.getValue().description, false);
                    }
                }
                //output specific help if command is specified
                else if(args.length == 2 && commandMap.containsKey(args[1]))
                {
                    builder.withTitle("I'm here to help! This is what I know about that.");

                    builder.appendField(commandMap.get(args[1]).commandName, commandMap.get(args[1]).description, false);
                }
                BotUtils.SendEmbed(event.getChannel(), builder.build());
            }
        };

        //addition of commands to hashmap
        commandMap.put(testCommand.commandName, testCommand);
        commandMap.put(helpCommand.commandName, helpCommand);
    }

    //execute a command when the appropriate command is typed
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //store variables to easily access later
        String messageContent = event.getMessage().getContent();
        String[] commandArgs = messageContent.substring(1).split(" ");

        //if the command is a command
        if(messageContent.startsWith(BotUtils.BOT_PREFIX) && commandMap.containsKey(commandArgs[0]))
        {
            Command toExecute = commandMap.get(commandArgs[0]);

            //If the command is provided with invalid arguments, output a help for it
            if(commandArgs.length == 1 && toExecute.takesArgs || commandArgs.length == 2 && !toExecute.takesArgs)
            {
                //TODO - Provide text with usages for help to use the command
                BotUtils.SendMessage(event.getChannel(), "This is debug text for incorrectly providing arguments");
            }

            //execute if commands are valid
            if(commandArgs.length == 1 && !toExecute.takesArgs)
            {
                toExecute.Execute(event, null);
            }
            if(commandArgs.length == 2 && toExecute.takesArgs)
            {
                toExecute.Execute(event, commandArgs);
            }
        }
    }
}
