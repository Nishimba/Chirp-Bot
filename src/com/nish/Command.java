package com.nish;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/*
 * Created by Nishimba on 08/01/19
 * A simple command class that all other commands can be built off of
 */

abstract class Command
{
    //description and name for help
    private String description;
    String commandName;

    //initiates the command
    Command(String name, String desc)
    {
        description = desc;
        commandName = name;
    }

    //execution to be filled in when creating a command
    abstract void Execute(MessageReceivedEvent event);
}
