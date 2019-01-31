package com.dbase;

import sx.blah.discord.handle.obj.IGuild;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/*
 * Created by Nishimba on 30/01/19
 * the utilities to be used for VOD database operations
 */

class VODUtils
{
    //connection to the mysql server
    private Connection VODConn;

    //create the connection, and create tables for servers if the servers dont have tables yet
    VODUtils(Connection conn, List<IGuild> guilds)
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

    //Add VOD info
    private void AddVODRecord(List<Object> params)
    {

    }
}
