package com.dbase;

import com.nish.BotUtils;
import com.nish.YTParser;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * Created by Nishimba on 30/01/19
 * the utilities to be used for VOD database operations
 */

public class VODUtils
{
    //connection to the mysql server
    private static Connection VODConn;

    //create the connection, and create tables for servers if the servers dont have tables yet
    public VODUtils(Connection conn, List<IGuild> guilds)
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
    public static Integer AddVODRecord(ArrayList<Object> params, MessageReceivedEvent event)
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

    static ArrayList<Object> ValidateVODInfo(MessageReceivedEvent event, String[] args)
    {
        String errorString = "";
        boolean hasError = false;
        ArrayList<Object> correctedList = new ArrayList<>();

        //convert the array to string list
        ArrayList<String> tempList = new ArrayList<>(Arrays.asList(args));
        tempList.remove(0);
        StringBuilder builder = new StringBuilder();
        for(String s : tempList)
        {
            builder.append(s);
        }
        String temp = builder.toString();

        args = temp.split(",");
        //try catch in case the SR is not a valid number
        try
        {
            //if the SR is invalid
            if(Integer.parseInt(args[0]) < 0 || Integer.parseInt(args[0]) > 5000)
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

        String heroCheck = BotUtils.ListCompare(BotUtils.ReadLines("res/Heroes.txt"), args[1], 0.7);
        if(heroCheck == null)
        {
            errorString += args[1] + " is not a valid hero, try resubmitting the command with a valid hero, you may have made a spelling mistake.\n";
            hasError = true;
        }
        correctedList.add(heroCheck);//add hero if hero is valid

        String mapCheck = BotUtils.ListCompare(BotUtils.ReadLines("res/Maps.txt"), args[2], 0.7);
        if(mapCheck == null)
        {
            errorString += args[2] + " is not a valid map, try resubmitting the command with a valid map, you may have made a spelling mistake.\n";
            hasError = true;
        }
        correctedList.add(mapCheck);//add map if map is valid

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
            return correctedList;
        }
        else
        {
            BotUtils.SendMessage(event.getChannel(), errorString);
            return null;
        }
    }
}
