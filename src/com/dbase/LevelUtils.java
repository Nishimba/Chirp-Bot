package com.dbase;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;

class LevelUtils
{
    private static Connection levelConn;
    private static List<IGuild> guildList;

    public static double[] levelBarriers = new double[100];

    LevelUtils(Connection conn, List<IGuild> guilds)
    {
        levelConn = conn;
        guildList = guilds;
        CreateLevelsDB();
    }

    private void CreateLevelsDB()
    {
        try
        {
            Statement createStmt = levelConn.createStatement();

            for (IGuild guild : guildList)
            {
                String guildID = guild.getStringID();
                String createLevelsTable = "CREATE TABLE IF NOT EXISTS levels_" + guildID + "(" +
                        "UserID BIGINT NOT NULL," +
                        "Level INT NOT NULL," +
                        "XPAmount int NOT NULL," +
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

    public static String PopulateLevelBarriers()
    {
        // Populate
        levelBarriers[0] = 250;
        for (int i = 1; i < 100; i++)
        {
            levelBarriers[i] = levelBarriers[i - 1] + (((4.0 * i) / 5.0) * (Math.pow(i, 3.0 / 2.0) ) + 250);
        }

        // Structure Output
        String output = "";
        for(int i = 0; i < 10; i++)
        {
            output += i + ": " + (int)levelBarriers[i] + "\n";
        }

        System.out.println("we did it nigger");
        return output;
    }

    public static void allocateXP(MessageReceivedEvent event)
    {
        // Not a bot
        if (!event.getAuthor().isBot())
        {
            // TODO Minute timer

            // add xp (15 - 25)
            Random rand = new Random();
            addXP(rand.nextInt(11) + 15, event.getAuthor(), event.getGuild());
        }
    }

    private static void addXP(int amount, IUser user, IGuild guild)
    {
        try
        {
            Statement createStmt = levelConn.createStatement();

            // Get current XP of message author
            int currentXP = getCurrentXP(user, guild);

            // TODO Multipliers

            int newXP = currentXP + amount;

            String updateTable = "UPDATE levels_" + guild.getStringID() + " SET XPAmount = "+ newXP + " WHERE UserID=" + user.getStringID() + ";";

            createStmt.execute(updateTable);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static int getCurrentXP(IUser user, IGuild guild)
    {
        try
        {
            Statement createStmt = levelConn.createStatement();

            String guildID = guild.getStringID();
            String authorID = user.getStringID();
            String getUserXP = "SELECT XPAmount FROM levels_" + guildID + " WHERE UserID=" + authorID + ";";

            ResultSet userXPSet = createStmt.executeQuery(getUserXP);

            if(!userXPSet.next() && !user.isBot()) {
                System.out.println("Adding new user to table");
                String addUserToDB = "INSERT INTO levels_" + guildID + " (UserID, Level, XPAmount, XPMultiplier) VALUES (" + authorID + ", 1, 0, 1);";
                createStmt.execute(addUserToDB);
                userXPSet = createStmt.executeQuery(getUserXP);
                userXPSet.next();
            }
            return userXPSet.getInt(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }
    }
}
