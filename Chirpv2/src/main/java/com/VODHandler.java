package com;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VODHandler
{
    static Connection conn;
    Logger logger = LoggerFactory.getLogger("VOD Logger");

    public void setConn(Connection db)
    {
        conn = db;
    }

    Integer AddVODRecord(ArrayList<Object> params, MessageCreateEvent event)
    {
        logger.info("Adding VOD to database...");
        //addvod command takes params in this order: SR, Hero, Map, Youtube Link
        try
        {
            Statement insertStmt = conn.createStatement();

            String guildID = event.getGuildId().get().asString();
            String submitter = event.getMember().get().getDisplayName();

            String insertVOD = "INSERT INTO vod_" + guildID +"(" +
                    "SkillRate, Hero, Map, YouTubeLink, Submitter, TimeSubmitted) VALUES" +
                    "('" + params.get(0) + "','" +
                    params.get(1).toString() + "','" +
                    params.get(2).toString() + "','" +
                    params.get(3).toString() + "','" +
                    submitter + "','" +
                    Timestamp.from(event.getMessage().getTimestamp()) + "');";//timestamp

            if(insertStmt.execute(insertVOD))
            {
                //put the VOD into the database, and return the vod ID to the user
                Statement selectStmt = conn.createStatement();
                ResultSet VOD = selectStmt.executeQuery("SELECT LAST_INSERT_ID();");
                VOD.next();
                int VODID = VOD.getInt(1);
                logger.info("VOD successfully added with ID: " + VODID);
                return VODID;
            }
            else
            {
                logger.error("There was an error in VOD insertion. VOD could not be added.");
                return null;
            }
        }
        catch (SQLException e)
        {
            logger.error("There was an error in VOD insertion. VOD could not be added.");
            e.printStackTrace();
            return null;
        }
    }

    int DeleteVODRecord(int VODID, MessageCreateEvent event)
    {
        //delete vod removes a VOD based on its ID
        try
        {
            Statement deleteStmt = conn.createStatement();

            String guildID = event.getGuildId().get().asString();
            String deleteVOD = "DELETE FROM vod_" + guildID + " WHERE VOD_ID = '" + VODID + "';";
            return deleteStmt.executeUpdate(deleteVOD);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    void SelectVODRecord(int VODID, MessageCreateEvent event)
    {
        //delete vod removes a VOD based on its ID
        try
        {
            Statement selectStmt = conn.createStatement();

            String guildID = event.getGuildId().get().asString();
            String selectVOD = "SELECT * FROM vod_" + guildID + " WHERE VOD_ID = '" + VODID + "';";

            ResultSet results = selectStmt.executeQuery(selectVOD);
            results.next();

            if(results.getInt(1) == VODID)
            {
                event.getMessage().getChannel().flatMap(reply -> reply.createEmbed(spec ->
                        {
                            try
                            {
                                spec.setAuthor("Chirp Help", null, null)
                                .setTitle(event.getGuild().block().getMemberById(Snowflake.of(results.getLong(2))).block().getDisplayName() + "'s VOD")
                                .addField("SR", "" + results.getInt(5), true)
                                .addField("Hero", results.getString(3), true)
                                .addField("Map", results.getString(4),  true)
                                .addField("YouTube Link", results.getString(7), false);
                            } catch (SQLException e)
                            {
                                e.printStackTrace();
                            }
                        }
                ));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            BotUtils.SendMessage(event,"That VOD could not be found! Are you sure you entered the correct ID?");
        }
    }

    static ArrayList<Object> ValidateVODInfo(MessageCreateEvent event, String[] args)
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
            event.getMessage().getChannel().flatMap(reply -> reply.createEmbed(spec ->
                    {
                        spec.setAuthor("Chirp Help", null, null)
                                .setTitle(event.getMember().get().getDisplayName() + "'s VOD")
                                .addField("SR", "" + correctedList.get(0).toString(), true)
                                .addField("Hero", correctedList.get(1).toString(), true)
                                .addField("Map", correctedList.get(2).toString(),  true)
                                .addField("YouTube Link", correctedList.get(3).toString(), false);
                    }
            ));

            return correctedList;
        }
        else
        {
            String finalErrorString = errorString;
            event.getMessage().getChannel().flatMap(reply -> reply.createMessage(finalErrorString));
            return null;
        }
    }
}
