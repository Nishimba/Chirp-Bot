package com.nish;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

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
            void Execute(MessageReceivedEvent event)
            {
                BotUtils.SendMessage(event.getChannel(), "Tweet Tweet");
            }
        };

        //addition of commands to hashmap
        commandMap.put(testCommand.commandName, testCommand);
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
                toExecute.Execute(event);
            }
            if(commandArgs.length == 2 && toExecute.takesArgs)
            {
                toExecute.Execute(event);
            }
        }
    }
}
