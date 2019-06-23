package com.dbase;

import com.nish.BotUtils;
import com.nish.YTParser;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by Nishimba on 30/01/19
 * the utilities to be used for VOD database operations
 */

class VODUtils
{
    //connection to the mysql server
    private static Connection VODConn;

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

    //Add VOD info, returns VODID if added correctly, and NULL if not added
    static Integer AddVODRecord(ArrayList<Object> params, MessageReceivedEvent event)
    {
        //addvod command takes params in this order: SR, Hero, Map, Youtube Link
        try
        {
            Statement insertStmt = VODConn.createStatement();

            String guildID = event.getGuild().getStringID();
            String insertVOD = "INSERT INTO vod_" + guildID +"(" +
                    "SkillRate, Hero, Map, YouTubeLink, Submitter, TimeSubmitted) VALUES" +
                    "('" + params.get(0) + "','" +
                    params.get(1).toString() + "','" +
                    params.get(2).toString() + "','" +
                    params.get(3).toString() + "','" +
                    event.getAuthor().getStringID() + "','" +
                    Timestamp.from(event.getMessage().getTimestamp()) + "');";//timestamp

            insertStmt.execute(insertVOD);

            //put the VOD into the database, and return the vod ID to the user
            Statement selectStmt = VODConn.createStatement();
            ResultSet VODID = selectStmt.executeQuery("SELECT LAST_INSERT_ID();");
            VODID.next();
            return VODID.getInt(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    static int DeleteVODRecord(int VODID, MessageReceivedEvent event)
    {
        //delete vod removes a VOD based on its ID
        try
        {
            Statement deleteStmt = VODConn.createStatement();

            String guildID = event.getGuild().getStringID();
            String deleteVOD = "DELETE FROM vod_" + guildID + " WHERE VOD_ID = '" + VODID + "';";
            return deleteStmt.executeUpdate(deleteVOD);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    static void SelectVODRecord(int VODID, MessageReceivedEvent event)
    {
        //delete vod removes a VOD based on its ID
        try
        {
            Statement selectStmt = VODConn.createStatement();

            String guildID = event.getGuild().getStringID();
            String selectVOD = "SELECT * FROM vod_" + guildID + " WHERE VOD_ID = '" + VODID + "';";

            ResultSet results = selectStmt.executeQuery(selectVOD);
            results.next();

            if(results.getInt(1) == VODID)
            {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.withAuthorName("Chirp Help");
                embedBuilder.withTitle(event.getGuild().getUserByID(results.getLong(2)).getName() + "'s VOD");
                embedBuilder.appendField("SR", "" + results.getInt(5), true);
                embedBuilder.appendField("Hero", results.getString(3), true);
                embedBuilder.appendField("Map", results.getString(4), true);
                embedBuilder.appendField("YouTube Link", results.getString(7), false);

                BotUtils.SendEmbed(event.getChannel(), embedBuilder.build());
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            BotUtils.SendMessage(event.getChannel(), "That VOD could not be found! Are you sure you entered the correct ID?");
        }
    }

    static ArrayList<Object> ValidateVODInfo(MessageReceivedEvent event, String[] args)
    {
        String errorString = "";
        boolean hasError = false;
        ArrayList<Object> correctedList = new ArrayList<>();

        args = BotUtils.convertArgsToList(args);
        if(args.length != 4)
        {
            return null;
        }

        //try catch in case the SR is not a valid number
        try
        {
            //if the SR is invalid
            int SR = Integer.parseInt(args[0]);
            if(SR < 0 || SR > 5000)
            {
                errorString += "SR must be a number between 0 and 5000.\n";
                hasError = true;
            }
        }
        catch (NumberFormatException e)//if SR is not a number this will error
        {
            errorString += "SR must be a number.\n";
            hasError = true;
        }
        catch (NullPointerException e)
        {
            hasError = true;
        }
        correctedList.add(args[0]);//add SR to valid list

        //read heroes and do spellchecking
        List<String> heroesList = BotUtils.ReadLines("res/Heroes.txt");
        String heroCheck = null;
        if(heroesList != null)
        {
             heroCheck = BotUtils.ListCompare(heroesList, args[1], 0.7);
        }
        else
        {
            errorString += "Error reading heroes file, please ask staff for help!";
            hasError = true;
        }

        if(heroCheck == null && heroesList != null)
        {
            errorString += args[1] + " is not a valid hero, try resubmitting the command with a valid hero, you may have made a spelling mistake.\n";
            hasError = true;
        }
        correctedList.add(heroCheck);//add hero if hero is valid

        //Read maps and do spellchecking
        List<String> mapsList = BotUtils.ReadLines("res/Maps.txt");
        String mapCheck = null;
        if (mapsList != null)
        {
            mapCheck = BotUtils.ListCompare(mapsList, args[2], 0.7);
        }
        else
        {
            errorString += "Error reading heroes file, please ask staff for help!";
            hasError = true;
        }

        if(mapCheck == null && mapsList != null)
        {
            errorString += args[2] + " is not a valid map, try resubmitting the command with a valid map, you may have made a spelling mistake.\n";
            hasError = true;
        }
        correctedList.add(mapCheck);//add map if map is valid

        //check youtube link with youtube parser
        YTParser parser = new YTParser();
        boolean validLink = parser.queryAPI(args[3]);
        if(!validLink)
        {
            errorString += "The YouTube link you submitted was invalid, please submit a valid one.\n";
            hasError = true;
        }
        correctedList.add(args[3]);

        if(!hasError)
        {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.withAuthorName("Chirp Help");
            embedBuilder.withTitle(event.getAuthor().getName() + "'s VOD");
            embedBuilder.appendField("SR", correctedList.get(0).toString(), true);
            embedBuilder.appendField("Hero", correctedList.get(1).toString(), true);
            embedBuilder.appendField("Map", correctedList.get(2).toString(), true);
            embedBuilder.appendField("YouTube Link", correctedList.get(3).toString(), false);

            BotUtils.SendEmbed(event.getChannel(), embedBuilder.build());

            return correctedList;
        }
        else
        {
            BotUtils.SendMessage(event.getChannel(), errorString);
            return null;
        }
    }
}
