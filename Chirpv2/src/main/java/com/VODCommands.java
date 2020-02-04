package com;

import discord4j.command.Command;
import discord4j.core.DiscordClient;

import java.util.HashMap;

public class VODCommands
{
    DiscordClient cli;

    VODCommands(DiscordClient client, HashMap<String, Command> commanpMap)
    {
        cli = client;
        //InitiateCommands(commanpMap);
    }


}
