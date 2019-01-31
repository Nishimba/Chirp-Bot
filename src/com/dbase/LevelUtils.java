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
        CreateLevelsDB(guilds);
    }

    private void CreateLevelsDB(List<IGuild> guilds)
    {
        try
        {
            Statement createStmt = levelConn.createStatement();

            for (IGuild guild:guilds)
            {
                String guildID = guild.getStringID();
                String createLevelsTable = "CREATE TABLE IF NOT EXISTS levels_" + guildID + "(" +
                        "UserID BIGINT NOT NULL," +
                        "Level INT NOT NULL," +
                        "XPAmount int NOT NULL," +
                        "TimeStamp DATETIME," +
                        "XPMultiplier DOUBLE," +
                        "PRIMARY KEY(UserID));";

                createStmt.execute(createLevelsTable);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
