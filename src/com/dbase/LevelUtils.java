package com.dbase;

import com.nish.BotUtils;
import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

public class LevelUtils
{
    //Const Variables
    public static final double MIN_XP_PER_MESSAGE = 15.0;
    public static final double MAX_XP_PER_MESSAGE = 25.0;

    //Flexible Variables
    private static Connection levelConn;
    private static List<IGuild> guildList;

    private static int gifDelayTime = 0;

    //Level up barriers
    private static double[] levelBarriers = new double[502];

    //Dictionary to store time of last msg for each user
    private static Map<IUser, Instant> lastMsg = new HashMap<>();

    LevelUtils(Connection conn, List<IGuild> guilds)
    {
        levelConn = conn;
        guildList = guilds;
        CreateLevelsDB();
        CreateRolesDB();
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
                        "XPAmount DOUBLE(15,2) NOT NULL," +
                        "PRIMARY KEY(UserID));";

                createStmt.execute(createLevelsTable);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void CreateRolesDB()
    {
        try
        {
            Statement createStmt = levelConn.createStatement();

            for (IGuild guild : guildList)
            {
                String guildID = guild.getStringID();
                String createRolesTable = "CREATE TABLE IF NOT EXISTS roles_" + guildID + "(" +
                        "RoleID BIGINT NOT NULL," +
                        "LevelCutoff INT," +
                        "XPMultiplier DOUBLE," +
                        "IsMOTM BOOLEAN DEFAULT FALSE," +
                        "PRIMARY KEY(RoleID));";

                createStmt.execute(createRolesTable);
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
            levelBarriers[i] = Math.floor(levelBarriers[i - 1] + (((4.0 * (i - 1)) / 5.0) * (Math.pow((i - 1), 3.0 / 2.0) ) + 250.0));
        }

        for(int i = 100; i <501; i++)
        {
            levelBarriers[i] = levelBarriers[i-1] + 76309; //TODO Remove this hard coded number. What is the significance of this value exactly?
        }

        levelBarriers[501] = Integer.MAX_VALUE;
    }

    private static int getRank(IUser user, IGuild guild)
    {
        try
        {
            Statement createStmt = levelConn.createStatement();

            String query = "SELECT a.RowNum FROM (SELECT ROW_NUMBER() OVER(ORDER BY XPAmount DESC) AS RowNum, UserID, Level, XPAmount FROM levels_" + guild.getStringID() + ") a WHERE UserID=" + user.getStringID() + ";";

            ResultSet results = createStmt.executeQuery(query);

            results.next();

            return results.getInt(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    static void updateRoles(IUser user, IGuild guild)
    {
        try
        {
            Statement createStmt = levelConn.createStatement();

            int currentLevel = getCurrentLevel(user, guild);
            int tempLevel = currentLevel%100;
            if(tempLevel == 0 || currentLevel == 501)
            {
                tempLevel = 100;
            }

            String query;
            long roleIDToAdd = - 1;

            if(currentLevel != 501)
            {
                query = "SELECT RoleID FROM roles_" + guild.getStringID() + " WHERE LevelCutoff <=" + tempLevel +" ORDER BY LevelCutoff DESC;";

                ResultSet results = createStmt.executeQuery(query);

                if(results.next())
                {
                    roleIDToAdd = results.getLong(1);
                    user.addRole(guild.getRoleByID(roleIDToAdd));
                }
            }

            query = "SELECT RoleID FROM roles_" + guild.getStringID() + " WHERE LevelCutoff<=" + currentLevel +" ORDER BY LevelCutoff DESC;";
            ResultSet results3 = createStmt.executeQuery(query);

            long prestigeRole = -1;
            if(results3.next())
            {
                prestigeRole = results3.getLong(1);
                user.addRole(guild.getRoleByID(results3.getLong(1)));
            }

            query = "SELECT RoleID FROM roles_" + guild.getStringID() + ";";

            ResultSet results2 = createStmt.executeQuery(query);

            List<IRole> userRoles = guild.getRolesForUser(user);

            if(roleIDToAdd != -1)
            {
                userRoles.remove(guild.getRoleByID(roleIDToAdd));
            }
            if(prestigeRole != -1)
            {
                userRoles.remove(guild.getRoleByID(prestigeRole));
            }

            while(results2.next())
            {
                if(userRoles.contains(guild.getRoleByID(results2.getLong(1))))
                {
                    user.removeRole(guild.getRoleByID(results2.getLong(1)));
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    static void addRoleToDB(IRole role, IGuild guild, int level)
    {
        try
        {
            Statement createStmt = levelConn.createStatement();

            String query = "INSERT INTO roles_" + guild.getStringID() + " VALUES (" + role.getStringID() + ", ";

            //If the level given is set to 0, make the level cutoff value NULL. (Effectively disabling the level cutoff)
            if (level == 0)
            {
                query += "NULL";
            }
            else
            {
                query += level;
            }

            query += ", " + 1.0 + ", FALSE) ON DUPLICATE KEY UPDATE LevelCutoff='" + level + "';";
            createStmt.execute(query);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    static void allocateXP(MessageReceivedEvent event)
    {
        // Not a bot
        if (!event.getAuthor().isBot() && !event.getMessage().getContent().substring(0, 1).equals(BotUtils.BOT_PREFIX))
        {
            // Check if 1min has passed since last msg
            if(checkCooldown(event))
            {
                // Get Multipliers for user
                double totalMultiplier = 1.0;
                for(IRole role : event.getAuthor().getRolesForGuild(event.getGuild()))
                {
                    totalMultiplier *= getMultiplierForRole(event.getGuild(), role);
                }

                // Add random XP between min and max by multipliers
                addXP((getRandomIntegerBetweenRange(MIN_XP_PER_MESSAGE, MAX_XP_PER_MESSAGE) * totalMultiplier), event.getAuthor(), event.getGuild());
            }
        }
    }

    static void addXP(double amount, IUser user, IGuild guild)
    {
        try
        {
            Statement createStmt = levelConn.createStatement();

            // Get current XP of message author
            double currentXP = getCurrentXP(user, guild);

            double newXP = currentXP + amount;

            if(newXP < 0)
            {
                newXP = 0;
            }

            if(newXP > (int)levelBarriers[501] - 1)
            {
                newXP = (int)levelBarriers[501] - 1;
            }

            // Update SQL Table with new XP
            String updateXP = "UPDATE levels_" + guild.getStringID() + " SET XPAmount = "+ newXP + " WHERE UserID=" + user.getStringID() + ";";
            createStmt.execute(updateXP);

            // Check for level up
            int localLevel = calculateCurrentLevel(user, guild);
            int dbLevel = getCurrentLevel(user, guild);

            if(dbLevel != localLevel)
            {
                // DM user level up message
                String msg = "You have leveled up in " + guild.getName() + "!";

                if(localLevel > 500)
                {
                    msg += " You are now MAXED! " + guild.getEmojiByName("gemDiamond");
                }
                else
                {
                    msg +=  " You are now level " + localLevel + "!";
                    switch (localLevel)
                    {
                        case 101:
                            msg += " " + guild.getEmojiByName("gemTopaz");
                            break;
                        case 201:
                            msg += " " + guild.getEmojiByName("gemRuby");
                            break;
                        case 301:
                            msg += " " + guild.getEmojiByName("gemSapphire");
                            break;
                        case 401:
                            msg += " " + guild.getEmojiByName("gemEmerald");
                            break;
                        default:
                            break;
                    }
                }

                BotUtils.SendMessage(user.getOrCreatePMChannel(), msg);

                // Update SQL Table with new level
                String updateLevel = "UPDATE levels_" + guild.getStringID() + " SET Level = "+ localLevel + " WHERE UserID=" + user.getStringID() + ";";
                createStmt.execute(updateLevel);

                updateRoles(user, guild);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private static double addNewUserToDB(ResultSet newUserSet, IUser user, IGuild guild, Statement createStmt, String sqlQuery)
    {
        String userID = user.getStringID();
        String guildID = guild.getStringID();

        try
        {
            if(!newUserSet.next() && !user.isBot()) {
                System.out.println("Adding new user to table");
                String addUserToDB = "INSERT INTO levels_" + guildID + " (UserID, Level, XPAmount) VALUES (" + userID + ", 1, 0);";
                createStmt.execute(addUserToDB);
                newUserSet = createStmt.executeQuery(sqlQuery);
                newUserSet.next();
            }

            return newUserSet.getDouble(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    static double getCurrentXP(IUser user, IGuild guild)
    {
        try
        {
            Statement createStmt = levelConn.createStatement();
            String getUserXP = "SELECT XPAmount FROM levels_" + guild.getStringID() + " WHERE UserID=" + user.getStringID() + ";";
            ResultSet userXPSet = createStmt.executeQuery(getUserXP);

            return addNewUserToDB(userXPSet, user, guild, createStmt, getUserXP);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    static int getCurrentLevel(IUser user, IGuild guild)
    {
        try
        {
            Statement createStmt = levelConn.createStatement();
            String getUserLevel = "SELECT Level FROM levels_" + guild.getStringID() + " WHERE UserID=" + user.getStringID() + ";";
            ResultSet userLevelSet = createStmt.executeQuery(getUserLevel);

            //Down-cast double to return as an int
            //E.g. 6.9 returns 6;
            return (int) addNewUserToDB(userLevelSet, user, guild, createStmt, getUserLevel);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    private static int calculateCurrentLevel(IUser user, IGuild guild)
    {
        double xp = getCurrentXP(user, guild);
        int level = 0;

        for (double levelBarrier : levelBarriers)
        {
            if (xp >= levelBarrier)
            {
                level++;
            }
        }
        return level;
    }

    static double xpRequiredForLevel(int targetLevel, IUser user, IGuild guild)
    {
        if(targetLevel < 1)
        {
            targetLevel = 1;
        }
        if(targetLevel > 502)
        {
            targetLevel = 502;
        }

        double xp = getCurrentXP(user, guild);
        double xpNeeded = levelBarriers[targetLevel - 1];

        return xpNeeded - xp;
    }

    private static int xpDiffForLevel(int level)
    {
        int lowerXP = (int)levelBarriers[level - 1];
        int upperXP = (int)levelBarriers[level];

        return upperXP - lowerXP;
    }

    private static double xpProgress(int level, IUser user, IGuild guild)
    {
        int lowerXP = (int)levelBarriers[level - 1];

        return getCurrentXP(user, guild) - lowerXP;
    }

    private static boolean checkCooldown(MessageReceivedEvent event)
    {
        Instant eventInstant = event.getMessage().getTimestamp();
        IUser user = event.getAuthor();

        if(lastMsg.get(user) == null)
        {
            lastMsg.put(user, eventInstant);
            return true;
        }

        // time in seconds since last message by this user
        int timeDifference = (int)eventInstant.getEpochSecond() - (int)lastMsg.get(user).getEpochSecond();

        if(timeDifference < 60)
        {
            return false;
        }
        else
        {
            lastMsg.replace(user, lastMsg.get(user), eventInstant);
            return true;
        }
    }

    static void addMultiplier(IGuild guild, IRole role, double multiplier)
    {
        String guildID = guild.getStringID();
        String roleID = role.getStringID();

        try
        {
            Statement createStmt = levelConn.createStatement();
            createStmt.execute("INSERT INTO roles_" + guildID + " (RoleID, XPMultiplier) VALUES (" + roleID + ", " + multiplier + ") ON DUPLICATE KEY UPDATE XPMultiplier='" + multiplier + "';");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    static double getMultiplierForRole(IGuild guild, IRole role)
    {
        String guildID = guild.getStringID();

        try
        {
            Statement createStmt = levelConn.createStatement();
            ResultSet multiSet = createStmt.executeQuery("SELECT XPMultiplier FROM roles_" + guildID + " WHERE RoleID=" + role.getStringID() + ";");

            if(multiSet.next())
            {
                return multiSet.getDouble(1);
            }
            else
            {
                return 1.0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    static ResultSet topN(IGuild guild, int count)
    {
        String guildID = guild.getStringID();

        try
        {
            Statement createStmt = levelConn.createStatement();

            String getTop = "SELECT * FROM levels_" + guildID + " ORDER BY XPAmount DESC LIMIT " + count + ";";

            return createStmt.executeQuery(getTop);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static File createLeaderboardCard(MessageReceivedEvent event, IGuild guild, int count)
    {
        File output;

        ResultSet results = topN(guild, count);

        boolean userInSet = false;

        if(results != null)
        {
            int i = 1;
            try
            {
                while (results.next())
                {
                    if (results.getLong(i) == Long.parseLong(event.getAuthor().getStringID()))
                    {
                        userInSet = true;
                    }
                }
                results.beforeFirst();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        int height = (300 * count) + 300;

        if (userInSet)
        {
            height -= 300;
        }

        BufferedImage bigCard = new BufferedImage(1000, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = bigCard.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(38, 39, 43));
        g.fillRect(0, 0, 1000, height);

        try
        {
            int y = 0;
            if (results != null)
            {
                while(results.next())
                {
                    IUser user = event.getClient().getUserByID(results.getLong(1));
                    File outputAvatar = getAvatar(user);
                    Image avatar;
                    if (outputAvatar != null)
                    {
                        avatar = ImageIO.read(outputAvatar).getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                    }
                    else
                    {
                        return null;
                    }
                    g.drawImage(drawRankCard(user, guild, avatar), 0, y, null);
                    y += 300;
                }
                if(!userInSet)
                {

                    IUser user = event.getClient().getUserByID(event.getAuthor().getLongID());
                    File outputAvatar = getAvatar(user);
                    Image avatar;
                    if (outputAvatar != null)
                    {
                        avatar = ImageIO.read(outputAvatar).getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                    }
                    else
                    {
                        return null;
                    }
                    g.drawImage(drawRankCard(user, guild, avatar), 0, y, null);
                }
            }
            else
            {
                return null;
            }

            g.setColor(new Color(10, 10, 11));
            g.setStroke(new BasicStroke(10));
            g.drawRect(0, 0, 1000, height);

            int lineY = 300;

            for(int i = 0; i < count-1; i++)
            {
                g.drawLine(0, lineY, 1000, lineY);
                lineY+=300;
            }
            if(!userInSet)
            {
                g.drawLine(0, lineY, 1000, lineY);
            }

            // Construct image
            output = new File("res/bigCard.png");
            ImageIO.write(bigCard, "png", output);
            return output;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static File getAvatar(IUser user)
    {
        /*
        TODO figure out bugs with some types of Gifs (e.g. the one Daalekz is using with the bird going down a rope)
        this error is caused regardless of the URL acquision method (using this new code or the existing code)
        so it might be as a result of the GIF creation code rather than this method
        */

        try
        {
            InputStream is;
            URL avatarURL;
            URLConnection connection;

            //Split URL and file ending
            String[] splitURL = user.getAvatarURL().split("\\.");
            StringBuilder combinedURL = new StringBuilder();
            String fileEnding = "";

            for(int i = 0; i < splitURL.length; i++)
            {
                //If this isn't the last period in the URL, combine it all together.
                if(i == splitURL.length - 1)
                {
                    //This is the last period of the URL, so these characters are the file ending of the given URL
                    fileEnding = splitURL[i];
                }
                else
                {
                    combinedURL.append(splitURL[i]);
                    combinedURL.append(".");
                }
            }

            //Now we have split up the URL and acquired the file type, assess if it is a GIF avatar or not and re-add it to the filepath.
            if(!fileEnding.equals("gif"))
            {
                combinedURL.append("png");
                fileEnding = "png"; //Change file ending to png (so it's synchronous with what's set in the URL!)
            }
            else
            {
                combinedURL.append(fileEnding);
            }

            //Create URL object and add size query to it as well
            avatarURL = new URL(combinedURL.toString() + "?size=256");

            // 403 error avoided :sunglasses:
            connection = avatarURL.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.connect();

            is = connection.getInputStream();

            OutputStream os = new FileOutputStream(new File("res/avatar." + fileEnding));

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();

            return new File("res/avatar." + fileEnding);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static File createRankCard(IUser user, IGuild guild)
    {
        try
        {
            File output;
            BufferedImage card;
            File outputAvatar = getAvatar(user);

            if (outputAvatar != null)
            {
                if(outputAvatar.getCanonicalPath().substring(outputAvatar.getCanonicalPath().length() - 3).equals("png"))
                {
                    Image avatar = ImageIO.read(outputAvatar).getScaledInstance(200, 200, Image.SCALE_SMOOTH);

                    card = drawRankCard(user, guild, avatar);

                    // Construct image
                    output = new File("res/card.png");
                    ImageIO.write(card, "png", output);
                    return output;
                }
                else
                {
                    ArrayList<BufferedImage> gifFrames = getFrames(outputAvatar);
                    ArrayList<BufferedImage> cardGif = new ArrayList<>();
                    for(Image i : gifFrames)
                    {
                        cardGif.add(drawRankCard(user, guild, i));
                    }

                    output = makeGif(cardGif);

                    // Construct image
                    return output;
                }
            }
            else
            {
                return null;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static BufferedImage drawRankCard(IUser user, IGuild guild, Image img) throws IOException
    {
        int imageWidth = 1000;
        int imageHeight = 300;

        int userLevel = getCurrentLevel(user, guild);
        double xpProg = xpProgress(userLevel, user, guild);
        double xpDiff = xpDiffForLevel(userLevel);

        boolean isMaxed = userLevel == 501;

        // Medal icons - original gold medal from Serhii Mudruk
        int scale = 25;
        Image goldMedal = ImageIO.read(new File("res/medalgold.png")).getScaledInstance(4 * scale, 3 * scale, Image.SCALE_SMOOTH);
        Image silverMedal = ImageIO.read(new File("res/medalsilver.png")).getScaledInstance(4 * scale, 3 * scale, Image.SCALE_SMOOTH);
        Image bronzeMedal = ImageIO.read(new File("res/medalbronze.png")).getScaledInstance(4 * scale, 3 * scale, Image.SCALE_SMOOTH);
        scale = 45;
        Image topazGem = ImageIO.read(new File("res/gemTopaz.png")).getScaledInstance(scale, scale, Image.SCALE_SMOOTH);
        Image rubyGem = ImageIO.read(new File("res/gemRuby.png")).getScaledInstance(scale, scale, Image.SCALE_SMOOTH);
        Image sapphireGem = ImageIO.read(new File("res/gemSapphire.png")).getScaledInstance(scale, scale, Image.SCALE_SMOOTH);
        Image emeraldGem = ImageIO.read(new File("res/gemEmerald.png")).getScaledInstance(scale, scale, Image.SCALE_SMOOTH);
        Image diamondGem = ImageIO.read(new File("res/gemDiamond.png")).getScaledInstance(scale, scale, Image.SCALE_SMOOTH);

        Color backgroundColor = new Color(38, 39, 43);
        Color outlineColor = Color.BLACK;
        Color textColor = new Color(240, 240, 240);
        Color secondTextColor = new Color(110, 110, 110);
        Color thirdTextColor = new Color(0.2f, 0.2f, 0.2f, 0.4f);
        Color highlightColor = new Color(user.getColorForGuild(guild).getRed(), user.getColorForGuild(guild).getGreen(), user.getColorForGuild(guild).getBlue());

        if(highlightColor.equals(Color.BLACK))
        {
            highlightColor = textColor;
        }

        String fontName = "";

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D rankGraphic = image.createGraphics();
        rankGraphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        rankGraphic.setColor(backgroundColor);
        rankGraphic.fillRoundRect(0, 0, imageWidth, imageHeight, 10, 10);

        // Avatar
        rankGraphic.drawImage(img, 50, 50, null);

        // Make Avatar Circle
        rankGraphic.setColor(backgroundColor);
        rankGraphic.setStroke(new BasicStroke(50));
        rankGraphic.drawOval(25, 25, 250, 250);

        // Outline Avatar
        rankGraphic.setStroke(new BasicStroke(6));
        rankGraphic.setColor(outlineColor);
        rankGraphic.drawOval(47, 47, 206, 206);

        // XP
        rankGraphic.setColor(secondTextColor);
        rankGraphic.setFont(new Font(fontName, Font.PLAIN, 30));
        String xpString = "";
        if(xpProg > 1000)
        {
            xpString += Math.round((xpProg/1000) * 100.0) / 100.0 + "K";
        }
        else
        {
            xpString += (int)xpProg;
        }
        xpString += " / ";
        if(xpDiff > 1000)
        {
            xpString += Math.round((xpDiff/1000) * 100.0) / 100.0 + "K";
        }
        else
        {
            xpString += (int)xpDiff;
        }
        xpString += " XP";
        if(isMaxed)
        {
            xpString = "MAXED";
        }
        int xXP = 930 - rankGraphic.getFontMetrics().stringWidth(xpString);
        rankGraphic.drawString(xpString, xXP, 180);

        // Username
        rankGraphic.setColor(textColor);
        String username = user.getName();
        int fontSize = 70;
        boolean shortenName = false;
        while(310 + rankGraphic.getFontMetrics(new Font(fontName, Font.PLAIN, 35)).stringWidth("#0000") + rankGraphic.getFontMetrics(new Font(fontName, Font.PLAIN, fontSize)).stringWidth(username) > xXP)
        {
            if (fontSize > 35)
            {
                fontSize --;
            }
            else
            {
                if(!shortenName)
                {
                    username = username.substring(0, username.length() - 1);
                    shortenName = true;
                }
                else
                {
                    username = username.substring(0, username.length() - 4);
                }
                username += "...";
            }
        }
        rankGraphic.setFont(new Font(fontName, Font.PLAIN, fontSize));
        rankGraphic.drawString(username, 300, 180);

        // Discriminator
        rankGraphic.setColor(textColor);
        rankGraphic.setFont(new Font(fontName, Font.PLAIN, 35));
        rankGraphic.drawString("#" + user.getDiscriminator(), rankGraphic.getFontMetrics(new Font(fontName, Font.PLAIN, fontSize)).stringWidth(username) + 310, 180);

        // Level
        int tempLevel = userLevel%100;
        if(tempLevel == 0)
        {
            tempLevel = 100;
        }
        if(isMaxed)
        {
            tempLevel = 500;
        }

        rankGraphic.setColor(highlightColor);
        rankGraphic.setFont(new Font(fontName, Font.PLAIN, 30));
        int xLevel = 930 - (rankGraphic.getFontMetrics().stringWidth("LEVEL") + rankGraphic.getFontMetrics(new Font(fontName, Font.PLAIN, 70)).stringWidth("" + tempLevel));
        rankGraphic.drawString("LEVEL", xLevel, 100);
        rankGraphic.setFont(new Font(fontName, Font.PLAIN, 70));
        rankGraphic.drawString("" + tempLevel, xLevel + 100, 100);

        // Rank
        rankGraphic.setColor(textColor);
        rankGraphic.setFont(new Font(fontName, Font.PLAIN, 30));
        int rank = getRank(user, guild);
        String rankString = "#" + rank;
        int xRank = xLevel - (rankGraphic.getFontMetrics().stringWidth("RANK") + rankGraphic.getFontMetrics(new Font(fontName, Font.PLAIN, 70)).stringWidth(rankString)) - 20;
        rankGraphic.drawString("RANK", xRank, 100);
        rankGraphic.setFont(new Font(fontName, Font.PLAIN, 70));
        rankGraphic.drawString(rankString, xRank + 90, 100);

        // Medal for rank
        rankGraphic.setColor(outlineColor);
        rankGraphic.setStroke(new BasicStroke(10));

        switch(rank)
        {
            case 1:
                rankGraphic.drawArc(204, 194, 52, 52, 70, 135);
                rankGraphic.drawImage(goldMedal, 180, 189, null);
                break;
            case 2:
                rankGraphic.drawArc(204, 194, 52, 52, 70, 135);
                rankGraphic.drawImage(silverMedal, 180, 189, null);
                break;
            case 3:
                rankGraphic.drawArc(204, 194, 52, 52, 70, 135);
                rankGraphic.drawImage(bronzeMedal, 180, 189, null);
                break;
            default:
                break;
        }

        // Gem for prestige
        switch((userLevel - 1)/100)
        {
            case 1:
                rankGraphic.drawArc(204, 54, 52, 52, 160, 135);
                rankGraphic.drawImage(topazGem, 205, 60, null);
                break;
            case 2:
                rankGraphic.drawArc(204, 54, 52, 52, 160, 135);
                rankGraphic.drawImage(rubyGem, 205, 60, null);
                break;
            case 3:
                rankGraphic.drawArc(204, 54, 52, 52, 160, 135);
                rankGraphic.drawImage(sapphireGem, 205, 60, null);
                break;
            case 4:
                rankGraphic.drawArc(204, 54, 52, 52, 160, 135);
                rankGraphic.drawImage(emeraldGem, 205, 60, null);
                break;
            case 5:
                rankGraphic.drawArc(204, 54, 52, 52, 160, 135);
                rankGraphic.drawImage(diamondGem, 205, 60, null);
                break;
            default:
                break;
        }


        // XP Bar Background
        rankGraphic.setColor(secondTextColor);
        rankGraphic.fillRoundRect(294, 204, 640, 32, 10, 10);

        // Fill XP Bar
        rankGraphic.setColor(highlightColor);
        double width = (xpProg / xpDiff) * 640;
        if(isMaxed)
        {
            width = 640;
        }
        rankGraphic.fillRoundRect(296, 204, (int)width, 32, 10, 10);

        // XP Separators
        rankGraphic.setColor(thirdTextColor);
        double x = 294.0;
        double amountOfSeps;
        int height;
        if(xpDiff <= 1000)
        {
            amountOfSeps = (xpDiff / 100.0);

            for(int i = 0; i < amountOfSeps; i++)
            {
                x += 640.0 / amountOfSeps;
                if(x < 294 + 640)
                {
                    rankGraphic.fillRect((int)x, 204, 5, 32);
                }
            }
        }
        else if(xpDiff < 3000)
        {
            amountOfSeps = (xpDiff / 1000.0);
            double amountOfLittleSeps = amountOfSeps * 10.0;

            for(int i = 0; i < amountOfLittleSeps; i++)
            {
                if((i + 1) % 10 == 0)
                {
                    height = 32;
                }
                else
                {
                    height = 12;
                }
                x += 640.0 / amountOfLittleSeps;
                if(x < 294 + 640)
                {
                    rankGraphic.fillRect((int)x, 204, 5, height);
                }
            }
        }
        else if(xpDiff < 10000)
        {
            amountOfSeps = (xpDiff / 1000.0);
            double amountOfLittleSeps = amountOfSeps * 4.0;

            for(int i = 0; i < amountOfLittleSeps; i++)
            {
                if((i + 1) % 4 == 0)
                {
                    height = 32;
                }
                else
                {
                    height = 12;
                }
                x += 640.0 / amountOfLittleSeps;
                if(x < 294 + 640)
                {
                    rankGraphic.fillRect((int)x, 204, 5, height);
                }
            }
        }
        else if(xpDiff < 30000)
        {
            amountOfSeps = (xpDiff / 10000.0);
            double amountOfLittleSeps = amountOfSeps * 10.0;

            for(int i = 0; i < amountOfLittleSeps; i++)
            {
                if((i + 1) % 10 == 0)
                {
                    height = 32;
                }
                else
                {
                    height = 12;
                }
                x += 640.0 / amountOfLittleSeps;
                if(x < 294 + 640)
                {
                    rankGraphic.fillRect((int)x, 204, 5, height);
                }
            }
        }
        else
        {
            amountOfSeps = (xpDiff / 10000.0);
            double amountOfLittleSeps = amountOfSeps * 4.0;

            for(int i = 0; i < amountOfLittleSeps; i++)
            {
                if((i + 1) % 4 == 0)
                {
                    height = 32;
                }
                else
                {
                    height = 12;
                }
                x += 640.0 / amountOfLittleSeps;
                if(x < 294 + 640)
                {
                    rankGraphic.fillRect((int)x, 204, 5, height);
                }
            }
        }

        // XP Bar Background - Part 2 Electric Boogaloo
        rankGraphic.setStroke(new BasicStroke(6));
        rankGraphic.setColor(backgroundColor);
        rankGraphic.drawRoundRect(290, 200, 650, 40, 10, 10);
        if(isMaxed)
        {
            rankGraphic.setColor(highlightColor);
            rankGraphic.setStroke(new BasicStroke(10));
            rankGraphic.fillRoundRect(296, 204, 640, 32, 10, 10);
        }
        rankGraphic.setStroke(new BasicStroke(4));
        rankGraphic.setColor(outlineColor);
        rankGraphic.drawRoundRect(294, 204, 642, 32, 10, 10);

        return image;
    }

    private static ArrayList<BufferedImage> getFrames(File gif) throws IOException
    {
        ArrayList<BufferedImage> frames = new ArrayList<>();

        ImageReader ir = new GIFImageReader(new GIFImageReaderSpi());

        ir.setInput(ImageIO.createImageInputStream(gif));

        // This shit was NOT fun - Gets the delay between frames from the gifs metadata so that we can reconstruct at the right frame rate
        gifDelayTime = Integer.parseInt(ir.getImageMetadata(0).getAsTree(ir.getImageMetadata(0).getNativeMetadataFormatName()).getChildNodes().item(1).getAttributes().getNamedItem("delayTime").getNodeValue());

        for(int i = 0; i < ir.getNumImages(true); i++)
        {
            Image temp = ir.read(i).getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            BufferedImage tempBuff = new BufferedImage(temp.getWidth(null), temp.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = tempBuff.createGraphics();
            g.drawImage(temp, null, null);
            frames.add(tempBuff);
        }

        return frames;
    }

    private static File makeGif(ArrayList<BufferedImage> frames) throws IOException
    {
        File outputFile = new File("res/card.gif");

        ImageOutputStream output = new FileImageOutputStream(outputFile);

        GifSequenceWriter writer = new GifSequenceWriter(output, frames.get(0).getType(), gifDelayTime, true);

        for(BufferedImage i : frames)
        {
            writer.writeToSequence(i);
        }

        writer.close();
        output.close();

        return outputFile;
    }

    static double getRandomIntegerBetweenRange(double min, double max)
    {
        return (int)(Math.random() * ((max-min)+1))+min;
    }

    static IRole selectMOTMRole(IGuild guild)
    {
        try {
            Statement createStmt = levelConn.createStatement();
            ResultSet motmRoles = createStmt.executeQuery("SELECT * FROM roles_" + guild.getStringID() + " WHERE IsMOTM = TRUE;");

            motmRoles.next();
            return guild.getRoleByID(motmRoles.getLong(1));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static void PrintMultipliers(IGuild guild, IUser user, IChannel channel)
    {
        //Create an embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.withTitle("Current Multipliers");

        //Check each role the user has and see if they have a special multiplier
        ArrayList<IRole> multiplierRoles = new ArrayList<>(); //Blank list of roles to store roles that actually give multipliers
        for(IRole role: user.getRolesForGuild(guild))
        {
            if(getMultiplierForRole(guild, role) != 1.0)
            {
                //Since this role does not have a standard 1.0 multiplier, we can add it to the list of roles the user has that gives multipliers
                multiplierRoles.add(role);
            }
        }

        //Check if there's any multiplier giving roles for the user and print them. If not, just state they do not have any roles.
        if(!multiplierRoles.isEmpty())
        {
            embed.appendField("Roles", "Here are a list of roles that grant XP multipliers: ", false);
            //Print these to the embed -- add each role to the embed and add what multiplier it gives
            for(IRole role: multiplierRoles)
            {
                embed.appendField(getMultiplierForRole(guild, role) + "x", role.mention(), true);
            }
        }
        else
        {
            //This user does not have any roles that grant multipliers
            embed.appendField("Roles", "You do not seem to have any roles that grant XP multipliers :(", false);
        }

        //Calculate the total XP rate of the user.
        double totalMultiplier = 1.0;
        for(IRole role: multiplierRoles)
        {
            totalMultiplier *= getMultiplierForRole(guild, role);
        }

        //Append the total multiplier of the current user.
        embed.appendField("Total XP Multiplier: " + totalMultiplier + "x", "You will earn between " + (MIN_XP_PER_MESSAGE * totalMultiplier) + "xp - " + (MAX_XP_PER_MESSAGE * totalMultiplier) + "xp per message.", false);

        //Build and send the embed
        BotUtils.SendEmbed(channel, embed.build());
    }

    static int toggleMOTM(IGuild guild, IRole role)
    {
         /*
            Return codes/values for the command:
            0 - MOTM role was successfully swapped
            1 - User is trying to enter a role as MOTM when one already exists
            2 - New role was set as MOTM
         */
        int returnStatus;

        //Check if a different role is entered as MOTM already
        try
        {
            Statement createStmt = levelConn.createStatement();
            ResultSet motmSet = createStmt.executeQuery("SELECT * FROM roles_" + guild.getStringID() + ";");
            boolean toggleMOTM = false;
            boolean preexistingMOTM = false;
            String roleString = "0";

            while(motmSet.next())
            {
                roleString = motmSet.getString("RoleID");

                if(motmSet.getBoolean("isMOTM"))
                {
                    //The role at this row is marked as the MOTM role.
                    if(!roleString.equals(role.getStringID()))
                    {
                        //The role marked with MOTM in the database is not the role being toggled (an MOTM role already exists)
                        //Therefore, we should inform the user a preexisting MOTM role exists and throw an error.
                        preexistingMOTM = true;
                        toggleMOTM = false;
                    }
                    else
                    {
                        //The user has entered the role that is already marked as MOTM -- so they want to toggle it.
                        toggleMOTM = true;
                    }
                }
            }


            //Toggle given role
            Statement toggleStmt = levelConn.createStatement();
            String roleUpdateStatement = "UPDATE roles_" + guild.getStringID() + " SET IsMOTM= ";

            if (!preexistingMOTM)
            {
                if (toggleMOTM)
                {
                    //This role is already marked as MOTM and the user wishes to toggle it.
                    roleUpdateStatement += "'0'";
                    returnStatus = 0;
                }
                else
                {
                    //This role is not MOTM, and is what the user wants to set as the MOTM role of the server/guild.
                    roleUpdateStatement += "'1'";
                    returnStatus = 2;
                }

                roleUpdateStatement += " WHERE (RoleID = '" + roleString + "');"; //Build last part of SQL query.
                toggleStmt.executeUpdate(roleUpdateStatement); //Execute given query.
            }
            else
            {
                //The role marked with MOTM in the database is not the role being toggled (an MOTM role already exists)
                //Therefore, we should inform the user a preexisting MOTM role exists and throw an error.
                returnStatus = 1;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return 0;
        }

        return returnStatus; //Return code generated.
    }
}
