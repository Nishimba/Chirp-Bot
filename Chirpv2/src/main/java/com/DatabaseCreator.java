package com;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseCreator
{
    Logger logger;
    Connection conn;

    public DatabaseCreator(DiscordClient cli)
    {
        logger = LoggerFactory.getLogger("Database Logger");
        logger.info("Beginning database setup...");

        try
        {
            List<String> login = BotUtils.ReadLines(("res/DBConfig.txt"));

            if(login != null)
            {
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/discord?serverTimezone=ACT&autoReconnect=true&failOverReadOnly=false&maxReconnects=10", login.get(0), login.get(1));
                setup(cli);
            }
            else
            {
                logger.error("Database login info could not be found. Shutting down.");
                System.exit(0);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            logger.error("Could not connect to the SQL server.");
        }
    }

    public void setup(DiscordClient cli)
    {
        CreateVODDB(cli);
        CreateLevelsDB(cli);
        CreateMultiDB(cli);
    }

    public void CreateVODDB(DiscordClient cli)
    {
        logger.info("Creating VOD database...");
        try
        {
            Statement createStmt = conn.createStatement();
            for(Guild guild : BotUtils.GetGuilds(cli))
            {
                String guildID = guild.getId().asString();
                String createVODTable = "CREATE TABLE IF NOT EXISTS vod_" + guildID + "(" +
                        "VOD_ID INT NOT NULL AUTO_INCREMENT," +
                        "Submitter VARCHAR(50) NOT NULL," +
                        "Hero VARCHAR(50) NOT NULL," +
                        "Map VARCHAR(50) NOT NULL," +
                        "SkillRate INT NOT NULL," +
                        "TimeSubmitted DATETIME NOT NULL," +
                        "YouTubeLink VARCHAR(50) NOT NULL," +
                        "PRIMARY KEY(VOD_ID));";

                if(!createStmt.execute(createVODTable))
                {
                    logger.info("VOD database successfully created!");
                }
                else
                {
                    logger.error("Error creating VOD database.");
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            logger.error("Error creating VOD database.");
        }

        //VODHandler.setConn(conn);
    }

    public void CreateLevelsDB(DiscordClient cli)
    {
        logger.info("Creating levels database...");
        try
        {
            Statement createStmt = conn.createStatement();

            for (Guild guild : BotUtils.GetGuilds(cli))
            {
                String guildID = guild.getId().asString();
                String createLevelsTable = "CREATE TABLE IF NOT EXISTS levels_" + guildID + "(" +
                        "UserID BIGINT NOT NULL," +
                        "Level INT NOT NULL," +
                        "XPAmount DOUBLE(10,2) NOT NULL," +
                        "PRIMARY KEY(UserID));";

                if(!createStmt.execute(createLevelsTable))
                {
                    logger.info("Levels database successfully created!");
                }
                else
                {
                    logger.error("Error creating levels database.");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Error creating levels database.");
        }
    }

    public void CreateMultiDB(DiscordClient cli)
    {
        logger.info("Creating multipliers database...");
        try
        {
            Statement createStmt = conn.createStatement();

            for (Guild guild : BotUtils.GetGuilds(cli))
            {
                String guildID = guild.getId().asString();
                String createRolesTable = "CREATE TABLE IF NOT EXISTS roles_" + guildID + "(" +
                        "RoleID BIGINT NOT NULL," +
                        "LevelCutoff INT," +
                        "XPMultiplier DOUBLE," +
                        "IsMOTM BOOLEAN DEFAULT FALSE," +
                        "PRIMARY KEY(RoleID));";

                if(!createStmt.execute(createRolesTable))
                {
                    logger.info("Multipliers database successfully created!");
                }
                else
                {
                    logger.error("Error creating multipliers database.");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Error creating multipliers database.");
        }
    }
}
