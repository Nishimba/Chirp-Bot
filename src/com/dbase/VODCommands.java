package com.dbase;

import com.nish.BotUtils;
import com.nish.Command;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class VODCommands
{
    public VODCommands(HashMap<String, Command> commandMap)
    {
        InitiateCommands(commandMap);
    }

    private void InitiateCommands(HashMap<String, Command> commandMap)
    {
        Command addVodCommand = new Command("addvod", "Adds a VOD to the database.", new String[]{"~addvod SR, Hero, Map, YT Link"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                ArrayList<Object> VODinfo = ValidateVODInfo(event, args);

                if(VODinfo != null)
                {
                    int VODID = VODUtils.AddVODRecord(VODinfo, event);
                    BotUtils.SendMessage(event.getChannel(), "Your VOD has been successfully added! The ID is: " + VODID +".");
                }
            }
        };

        commandMap.put(addVodCommand.commandName, addVodCommand);
    }

    ArrayList<Object> ValidateVODInfo(MessageReceivedEvent event, String[] args)
    {
        String errorString = "";
        boolean hasError = false;
        ArrayList<Object> correctedList = new ArrayList<>();

        //try catch in case the SR is not a valid number
        try
        {
            //if the SR is invalid
            if(Integer.parseInt(args[1]) < 0 || Integer.parseInt(args[1]) > 5000)
            {
                errorString += "SR must be a number between 0 and 5000.";
                hasError = true;
            }
        }
        catch (NumberFormatException e)//if SR is not a number this will error
        {
            errorString += "SR must be a number.";
            hasError = true;
        }
        catch (NullPointerException e)
        {
            hasError = true;
        }
        correctedList.add(args[1]);//add SR to valid list

        String heroCheck = BotUtils.StringFunnel("res/Heroes.txt", args[2]);
        if(heroCheck == null)
        {
            errorString += args[2] + "is not a valid hero, try resubmitting the command with a valid hero, you may have made a spelling mistake.";
            hasError = true;
        }
        correctedList.add(heroCheck);//add hero if hero is valid

        String mapCheck = BotUtils.StringFunnel("res/Maps.txt", args[3]);
        if(mapCheck == null)
        {
            errorString += args[3] + "is not a valid map, try resubmitting the command with a valid map, you may have made a spelling mistake.";
            hasError = true;
        }
        correctedList.add(mapCheck);//add map if map is valid

        //TODO: YOUTUBE LINK CHECKING HERE
        correctedList.add("YOUTUBE LINK");

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
