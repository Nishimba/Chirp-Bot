package com.nish;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/*
 * Created by Nishimba on 06/01/19
 */

public class MyEvents
{
    //What to do when a message is received on the server.
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //Simple test command
        if(event.getMessage().getContent().startsWith(BotUtils.BOT_PREFIX + "test"))
        {
            BotUtils.SendMessage(event.getChannel(), "I am testing this message");
        }
    }
}
