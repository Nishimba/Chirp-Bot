package com;

/*
 * Created by Nishimba on 08/01/19
 * A simple command class that all other commands can be built off of
 */

import discord4j.core.event.domain.message.MessageCreateEvent;

public abstract class Command
{
    String[] usages; //the ways that the command can be used
    protected String description; //the general description of the command
    public String commandName; //the name of the command. What it is called using.
    boolean isAdmin; //whether or not the command is only available to moderators

    //initiates the command
    protected Command(String name, String desc, String[] usage, boolean admin)
    {
        isAdmin = admin;
        description = desc;
        commandName = name;
        usages = usage;
    }

    //execution to be filled in when creating a command, returns a boolean when executes properly
    public abstract void Execute(MessageCreateEvent event, String[] args);
}
