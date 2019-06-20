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
            //uses login details to create the connection
            List<String> login = BotUtils.ReadLines("res/DBConfig.txt");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/discord?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=ACT", login.get(0), login.get(1));

            //creates the vod and level utilities
            new LevelUtils(conn, guilds);
            new VODUtils(conn, guilds);
        }
        catch (SQLException ex)
        {
            //a console log reminder for during the developing phase. Delete this line once the bot is complete.
            ex.printStackTrace();
            System.out.println("***************  Did you remember to start the sql server? ***************");
        }
    }
}
