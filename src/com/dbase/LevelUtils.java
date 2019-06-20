package com.dbase;

import sx.blah.discord.handle.obj.IGuild;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

class LevelUtils
{
    private Connection levelConn;

    LevelUtils(Connection conn, List<IGuild> guilds)
    {
        levelConn = conn;
        CreateLevelsDB(levelConn, guilds);
    }

    private void CreateLevelsDB(Connection conn, List<IGuild> guilds)
    {
        try
        {
            Statement createStmt = conn.createStatement();

            createStmt.executeQuery("USE discord");

            for (IGuild guild:guilds)
            {
                String guildID = guild.getStringID();
                String createServerTable = "CREATE TABLE IF NOT EXISTS Server_" + guildID + "(" +
                        "UserID BIGINT NOT NULL," +
                        "Level INT NOT NULL," +
                        "XPAmount int NOT NULL," +
                        "TimeStamp DATETIME," +
                        "XPMultiplier DOUBLE," +
                        "PRIMARY KEY(UserID));";

                createStmt.execute(createServerTable);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
