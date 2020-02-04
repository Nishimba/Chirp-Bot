package com;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;

import java.util.Objects;

public class BotCreator
{
    //Create the client to connect to the server
    DiscordClient getBuiltDiscordClient()
    {
        try
        {
            DiscordClient cli = new DiscordClientBuilder(Objects.requireNonNull(BotUtils.ReadLines("res/BotToken.txt")).get(0)).build();
            return cli;
        }
        catch (NullPointerException e)
        {
            System.out.println("Bot Token was null and could not connect, shutting down.");
            System.exit(0);
            return null;
        }
    }

    public void setup()
    {
        ChirpDBase();
    }
}
