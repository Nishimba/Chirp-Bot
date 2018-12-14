package com.DVod;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

/*
 * Created by Nishimba 14/12/18
 */

public interface Command
{
    void runCommand(MessageReceivedEvent event, List<String> args);
}
