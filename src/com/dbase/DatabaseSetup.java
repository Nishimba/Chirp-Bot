package com.dbase;

import com.nish.BotUtils;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.*;
import java.util.List;

/*
 * Created by Nishimba on 30/01/19
 * Setup for the database
 */

public class DatabaseSetup
{
    public DatabaseSetup(List<IGuild> guilds)
    {
        try
        {
            List<String> login = BotUtils.ReadLines("res/DBConfig.txt");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/discord?useSSL=false&allowPublicKeyRetrieval=true", login.get(0), login.get(1));
            new LevelUtils(conn, guilds);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
}
