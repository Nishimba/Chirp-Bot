package com.DVod;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.*;

/*
 * Created by Nishimba 14/12/18
 */

public class CommandHandler
{
    //static map of commands from string to func impl
    private static Map<String, Command> commandMap = new HashMap<>();

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //split the provided string on instances of a comma and space
        String[] argArray = event.getMessage().getContent().split(", ");

        //ensure that the command and prefix is present
        if(argArray.length == 0)
        {
            return;
        }

        //ensure that the the command starts with the prefix
        if(!argArray[0].startsWith(BotUtils.BOT_PREFIX))
        {
            return;
        }

        //get the command that was issued
        String commandStr = argArray[0].substring(BotUtils.BOT_PREFIX.length());

        //load the contents of the command into a list
        List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
        argsList.remove(0);

        //check if the command is valid and execute the command
        if(commandMap.containsKey(commandStr))
        {
            commandMap.get(commandStr).runCommand(event, argsList);
        }
        else
        {
            //invalid command message
            BotUtils.sendMessage(event.getChannel(), commandStr + "is not a valid command.");
        }
    }
}
