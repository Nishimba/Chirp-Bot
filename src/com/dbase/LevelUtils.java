package com.dbase;

import com.nish.BotUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;

public class LevelUtils
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

    public static void PopulateLevelBarriers()
    {
        levelBarriers[0] = 0;
        levelBarriers[1] = 250;
        for (int i = 2; i < 100; i++)
        {
            levelBarriers[i] = levelBarriers[i - 1] + (((4.0 * i) / 5.0) * (Math.pow(i, 3.0 / 2.0) ) + 250);
        }
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

    public static void addXP(int amount, IUser user, IGuild guild)
    {
        try
        {
            Statement createStmt = levelConn.createStatement();

            // Get current XP of message author
            int currentXP = getCurrentXP(user, guild);

            // TODO Multipliers

            int newXP = currentXP + amount;

            // Update SQL Table with new XP
            String updateXP = "UPDATE levels_" + guild.getStringID() + " SET XPAmount = "+ newXP + " WHERE UserID=" + user.getStringID() + ";";
            createStmt.execute(updateXP);

            // Check for level up
            int localLevel = calculateCurrentLevel(user, guild);
            int dbLevel = getCurrentLevel(user, guild);

            if(dbLevel != localLevel)
            {
                // DM user level up message
                BotUtils.SendMessage(user.getOrCreatePMChannel(), "You have leveled up in " + guild.getName() + "! You are now level " + localLevel + "!");

                // Update SQL Table with new level
                String updateLevel = "UPDATE levels_" + guild.getStringID() + " SET Level = "+ localLevel + " WHERE UserID=" + user.getStringID() + ";";
                createStmt.execute(updateLevel);
            }
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

    public static int getCurrentLevel(IUser user, IGuild guild)
    {
        try
        {
            Statement createStmt = levelConn.createStatement();

            String guildID = guild.getStringID();
            String authorID = user.getStringID();
            String getUserLevel = "SELECT Level FROM levels_" + guildID + " WHERE UserID=" + authorID + ";";

            ResultSet userLevelSet = createStmt.executeQuery(getUserLevel);

            if(!userLevelSet.next() && !user.isBot()) {
                System.out.println("Adding new user to table");
                String addUserToDB = "INSERT INTO levels_" + guildID + " (UserID, Level, XPAmount, XPMultiplier) VALUES (" + authorID + ", 1, 0, 1);";
                createStmt.execute(addUserToDB);
                userLevelSet = createStmt.executeQuery(getUserLevel);
                userLevelSet.next();
            }
            return userLevelSet.getInt(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public static int calculateCurrentLevel(IUser user, IGuild guild)
    {
        int xp = getCurrentXP(user, guild);
        int level = 0;

        for(int i = 0; i < levelBarriers.length; i++)
        {
            if(xp >= levelBarriers[i])
            {
                level++;
            }
        }
        return level;
    }

    public static int xpRequiredForLevel(int targetLevel, IUser user, IGuild guild)
    {
        int xp = getCurrentXP(user, guild);
        int xpNeeded = (int)levelBarriers[targetLevel - 1];

        return xpNeeded - xp + 1;
    }
}
