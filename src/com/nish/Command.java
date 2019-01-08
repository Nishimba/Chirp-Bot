package com.nish;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/*
 * Created by Nishimba on 08/01/19
 * A simple command class that all other commands can be built off of
 */

abstract class Command
{
    /*
     * Command parameters
     * ==================
     * CommandName: Self-explanatory, name of the command that will be input by the user for a certain result
     * Description: A description of the purpose (not the usage!) of the command, to be output within a help command
     * getUsage: An array of strings that output different usage examples of the command e.g (an example "ar" command):
     *          "ar create <tagname>      //creates tag",
     *          "ar regex <tag> <value>     //edit the regex of a tag",
     *          "ar response <tag> <value>  //change the response of a reply",
     *
     *  <> = required parameter
     *  [] = optional parameter
     *  <yes|no> = required parameter between two values
     */
    private String description;
    private String[] getUsage;
    String commandName;

    //initiates the command
    Command(String name, String desc, String[] usage)
    {
        description = desc;
        commandName = name;
        getUsage = usage;
    }

    //execution to be filled in when creating a command
    abstract void Execute(MessageReceivedEvent event);
}
