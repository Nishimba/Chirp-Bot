package com;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import org.slf4j.Logger;

import java.util.Objects;

public class BotCreator
{
    Logger logger;
    //Create the client to connect to the server
    DiscordClient getBuiltDiscordClient(Logger log)
    {
        logger = log;
        try
        {
            DiscordClient cli = new DiscordClientBuilder(Objects.requireNonNull(BotUtils.ReadLines("res/BotToken.txt")).get(0)).setInitialPresence(Presence.online(Activity.playing("Type ~help for help!"))).build();
            return cli;
        }
        catch (NullPointerException e)
        {
            logger.error("BotToken could not be found. Shutting down.");
            System.exit(0);
            return null;
        }
    }

    public void setup(DiscordClient cli)
    {
        for(Guild guild : BotUtils.GetGuilds(cli))
        {
            logger.info("Connected successfully to " + guild.getName() + "(" + guild.getId().asString() + ")");
        }
        DatabaseCreator dbCreator = new DatabaseCreator(cli);
        LevelsCreator levelCreator = new LevelsCreator(cli);
    }
}
