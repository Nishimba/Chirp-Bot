package com.DVod;

import sx.blah.discord.api.IDiscordClient;

import java.nio.file.Files;
import java.nio.file.Paths;

/*
 * Created by Nishimba 14/12/18
 */

public class Main
{
    //main method to run and initialise the bot
    public static void main(String[] args) throws Exception
    {
        //builds the bot with token from filepath
        IDiscordClient cli = BotUtils.getBuiltDiscordClient(getToken("BotToken.txt"));

        //handles commands
        cli.getDispatcher().registerListener(new CommandHandler());

        //logs bot in once initialised
        cli.login();
    }

    //gets the token from a file
    private static String getToken(String tokenPath) throws Exception
    {
        String token = "";
        token = new String(Files.readAllBytes(Paths.get(tokenPath)));
        return token;
    }
}
