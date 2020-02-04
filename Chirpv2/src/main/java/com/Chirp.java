package com;

import discord4j.core.DiscordClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chirp
{
    public static void main(String[] args)
    {
        Logger logger = LoggerFactory.getLogger("Setup Logger");
        logger.info("Starting Chirp...");
        BotCreator chirp = new BotCreator();
        DiscordClient cli = chirp.getBuiltDiscordClient(logger);

        chirp.setup(cli);

        cli.login().block();
    }
}
