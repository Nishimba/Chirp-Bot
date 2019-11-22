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
            if (login != null)
            {
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/discord?serverTimezone=ACT", login.get(0), login.get(1));

                //creates the vod and level utilities
                new LevelUtils(conn, guilds);
                new VODUtils(conn, guilds);
            }
            else
            {
                System.out.println("Login information for database could not be found, shutting down.");
                System.exit(0);
            }
        }
        catch (Exception ex)
        {
            //a console log reminder for during the developing phase. Delete this line once the bot is complete.
            ex.printStackTrace();
            System.out.println("***************  Did you remember to start the sql server? ***************");
        }
    }
}
