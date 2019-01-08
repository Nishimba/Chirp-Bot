package com.nish;

import org.jetbrains.annotations.NotNull;
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
        Command testCommand = new Command("test", "A simple test command", new String[] {"~test (No extra arguments)"})
        {
            void Execute(MessageReceivedEvent event)
            {
                BotUtils.SendMessage(event.getChannel(), "Tweet Tweet");
            }
        };

        //addition of commands to hashmap
        commandMap.put(testCommand.commandName, testCommand);
    }

    //execute a command when the approprite command is typed
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.getMessage().getContent().startsWith(BotUtils.BOT_PREFIX) && commandMap.containsKey(event.getMessage().getContent().substring(1)))
        {
            commandMap.get(event.getMessage().getContent().substring(1)).Execute(event);
        }
    }
}
