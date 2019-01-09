package com.nish;

import org.jetbrains.annotations.NotNull;
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
        Command testCommand = new Command("test", "A simple test command", new String[] {"~test (No extra arguments)"}, false)
        {
            void Execute(MessageReceivedEvent event, String[] args)
            {
                BotUtils.SendMessage(event.getChannel(), "Tweet Tweet");
            }
        };

        //The help command
        Command helpCommand = new Command("help", "A help command showing how to use Chirp.", false)
        {
            void Execute(MessageReceivedEvent event, String[] args)
            {
                //Create an embed
                EmbedBuilder builder = new EmbedBuilder();

                builder.withAuthorName("Chirp Help");

                builder.withTitle("I'm here to help! This is everything I know.\r\nFor help on a particular command, type that command!");

                //for each entry add it to the embed
                for (HashMap.Entry<String, Command> command: commandMap.entrySet())
                {
                    builder.appendField(command.getValue().commandName, command.getValue().description, false);
                }

                //send the embed
                BotUtils.SendEmbed(event.getChannel(), builder.build());
            }
        };

        //random args command
        Command argsCommand = new Command("args", "Argument commands", true)
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
            String[] commandArgs = messageContent.substring(1).split(" ");
            //if the command is a command
            if(messageContent.startsWith(BotUtils.BOT_PREFIX) && commandMap.containsKey(commandArgs[0]))
            {
                Command toExecute = commandMap.get(commandArgs[0]);

                //valid commands should execute
                if(toExecute.takesArgs && commandArgs.length > 1)
                {
                    toExecute.Execute(event, commandArgs);
                }
                if(!toExecute.takesArgs && commandArgs.length == 1)
                {
                    toExecute.Execute(event, null);
                }

                //invalid commands should output help text
                if((!toExecute.takesArgs && commandArgs.length > 1) || (toExecute.takesArgs  && commandArgs.length == 1))
                {
                    EmbedBuilder builder = new EmbedBuilder();

                    builder.withAuthorName("Chirp Help");

                    builder.withTitle("I'm here to help! This is everything I know about that.");
                    builder.appendField(toExecute.commandName, toExecute.description, false);

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
