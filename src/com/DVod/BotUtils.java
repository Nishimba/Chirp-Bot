package com.DVod;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

/*
 * Created by Nishimba 14/12/18
 */

class BotUtils
{
    //sets the prefix for bot commands
    static String BOT_PREFIX = "~";

    //constructs the discord client to interface with discord
    static IDiscordClient getBuiltDiscordClient(String token)
    {
        //actual builder for the client object
        return new ClientBuilder()
                .withToken(token)
                .withRecommendedShardCount()
                .build();
    }

    //makes sending messages easier
    static void sendMessage(IChannel channel, String message)
    {
        RequestBuffer.request(() -> {
            try
            {
                channel.sendMessage(message);
            }
            catch (DiscordException e)
            {
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
            }
        });
    }
}
