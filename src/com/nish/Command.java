package com.nish;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/*
 * Created by Nishimba on 08/01/19
 * A simple command class that all other commands can be built off of
 */

public abstract class Command
{
    String[] usages;//the ways that the command can be used
    protected String description;//the general description of the command
    public String commandName;//the name of the command. What it is called using.
	  boolean takesArgs;//whether or not the command takes arguments

    //initiates the command
    protected Command(String name, String desc, String[] usage, boolean args)
    {
        description = desc;
        commandName = name;
        usages = usage;
		    takesArgs = args;
    }

    //execution to be filled in when creating a command, returns a boolean when executes properly
    public abstract void Execute(MessageReceivedEvent event, String[] args);
}
