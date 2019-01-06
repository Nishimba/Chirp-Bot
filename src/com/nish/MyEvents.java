package com.nish;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class MyEvents
{
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.getMessage().getContent().startsWith(BotUtils.BOT_PREFIX + "test"))
        {
            BotUtils.SendMessage(event.getChannel(), "I am testing this message");
        }
    }
}
