package com.nish;

import sx.blah.discord.api.IDiscordClient;

import java.io.*;

public class MainRunner
{
    public static void main(String[] args) throws IOException
    {
        IDiscordClient cli = BotUtils.getBuiltDiscordClient(GetTokenFromFile("res/BotToken.txt"));

        cli.getDispatcher().registerListener(new MyEvents());

        cli.login();
    }

    private static String GetTokenFromFile(String tokenPath) throws IOException
    {
        BufferedReader tokenRead = new BufferedReader(new FileReader(tokenPath));
        return tokenRead.readLine();
    }
}
