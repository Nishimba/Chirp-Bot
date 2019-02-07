package com.dbase;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by Nishimba on 30/01/19
 * the utilities to be used for VOD database operations
 */

public class VODUtils
{
    //connection to the mysql server
    private static Connection VODConn;

    //create the connection, and create tables for servers if the servers dont have tables yet
    public VODUtils(Connection conn, List<IGuild> guilds)
    {
        VODConn = conn;
        CreateVODDB(guilds);
    }

    //create the vod tables if they dont exist
    private void CreateVODDB(List<IGuild> guilds)
    {
        try
        {
            Statement createStmt = VODConn.createStatement();

            for(IGuild guild : guilds)
            {
                String guildID = guild.getStringID();
                String createVODTable = "CREATE TABLE IF NOT EXISTS vod_" + guildID + "(" +
                        "VOD_ID INT NOT NULL AUTO_INCREMENT," +
                        "Submitter VARCHAR(50) NOT NULL," +
                        "Hero VARCHAR(50) NOT NULL," +
                        "Map VARCHAR(50) NOT NULL," +
                        "SkillRate INT NOT NULL," +
                        "TimeSubmitted DATETIME NOT NULL," +
                        "YouTubeLink VARCHAR(50) NOT NULL," +
                        "Feedback MEDIUMTEXT," +
                        "PRIMARY KEY(VOD_ID));";

                createStmt.execute(createVODTable);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //Add VOD info, returns VODID if added correctly, and NULL if not added
    public static Integer AddVODRecord(ArrayList<Object> params, MessageReceivedEvent event)
    {
        //addvod command takes params in this order: SR, Hero, Map, Youtube Link
        try
        {
            Statement insertStmt = VODConn.createStatement();

            String guildID = event.getGuild().getStringID();
            String insertVOD = "INSERT INTO vod_" + guildID +"(" +
                    "SkillRate, Hero, Map, YouTubeLink, Submitter, TimeSubmitted) VALUES" +
                    "('" + params.get(0) + "','" +
                    params.get(1).toString() + "','" +
                    params.get(2).toString() + "','" +
                    params.get(3).toString() + "','" +
                    event.getAuthor().getStringID() + "','" +
                    Timestamp.from(event.getMessage().getTimestamp()) + "');";//timestamp

            insertStmt.execute(insertVOD);

            //put the VOD into the database, and return the vod ID to the user
            Statement selectStmt = VODConn.createStatement();
            ResultSet VODID = selectStmt.executeQuery("SELECT LAST_INSERT_ID();");
            VODID.next();
            return VODID.getInt(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
