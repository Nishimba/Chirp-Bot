package com;

import discord4j.core.DiscordClient;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;

public class Chirp
{
    public static void main(String[] args)
    {
        BotCreator chirp = new BotCreator();
        DiscordClient cli = chirp.getBuiltDiscordClient();
        cli.login().block();

        chirp.setup();

        cli.updatePresence(Presence.online(Activity.playing("Type ~help for help!")));
    }
}
