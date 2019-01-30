package com.dbase;

import com.nish.BotUtils;

import java.sql.*;
import java.util.List;

/*
 * Created by Nishimba on 30/01/19
 * Setup for the database
 */

public class DatabaseSetup
{
    private Connection conn;

    public DatabaseSetup()
    {
        try
        {
            List<String> login = BotUtils.ReadLines("res/DBConfig.txt");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ebookshop?useSSL=false&allowPublicKeyRetrieval=true", login.get(0), login.get(1));

            Statement createStmt = conn.createStatement();

            createStmt.executeQuery("USE discord");

            String createServerTable = "CREATE TABLE IF NOT EXISTS Server_314011439406776320(" +
                    "UserID BIGINT NOT NULL," +
                    "Level INT NOT NULL," +
                    "XPAmount int NOT NULL," +
                    "TimeStamp DATETIME," +
                    "XPMultiplier DOUBLE," +
                    "PRIMARY KEY(UserID));";

            createStmt.execute(createServerTable);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
}
