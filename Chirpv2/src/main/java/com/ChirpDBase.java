package com;

import discord4j.core.object.entity.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class ChirpDBase
{
    Logger logger;

    public ChirpDBase(List<Guild> guilds)
    {
        logger = LoggerFactory.getLogger("Database");
        logger.info("Starting Database Setup");

        try
        {
            List<String> login = BotUtils.ReadLines(("res/DBConfig.txt"));

            if(login != null)
            {
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/discord?serverTimezone=ACT&autoReconnect=true&failOverReadOnly=false&maxReconnects=10", login.get(0), login.get(1));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            logger.error("Could not connect to the SQL server");
        }
    }
}
