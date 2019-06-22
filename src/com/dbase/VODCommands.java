package com.dbase;

import com.nish.BotUtils;
import com.nish.Command;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

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
                ArrayList<Object> VODinfo = VODUtils.ValidateVODInfo(event, args);

                if(VODinfo != null)
                {
                    int VODID = VODUtils.AddVODRecord(VODinfo, event);
                    event.getAuthor().getOrCreatePMChannel().sendMessage("Your VOD has the ID: " + VODID + ". Use this to delete or view the VOD.");
                }
                else
                {
                    EmbedBuilder builder = new EmbedBuilder();

                    builder.withAuthorName("Chirp Help");

                    //flavour text with the information about the command
                    builder.withTitle("You seemed to have used the addvod command incorrectly! I'm here to help - here's everything I know about addvod:");
                    builder.appendField(commandName, description, false);
                    builder.appendField("Example uses of " + (commandName), BotUtils.OutputUsage(commandName, commandMap), false);//output usages for the command

                    BotUtils.SendEmbed(event.getChannel(), builder.build());
                }
            }
        };

        Command delVodCommand = new Command("deletevod", "Removes a VOD from the database.", new String[]{"~deletevod ID"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    int success = VODUtils.DeleteVODRecord(Integer.parseInt(args[1]), event);
                    if(success == 1)
                    {
                        BotUtils.SendMessage(event.getChannel(), "Successfully deleted VOD!");
                    }
                    else
                    {
                        BotUtils.SendMessage(event.getChannel(), "VOD could not be found.");
                    }
                }
                catch (NumberFormatException e)
                {
                    BotUtils.SendMessage(event.getChannel(), "VOD ID must be a number.");
                }
            }
        };

        Command getVodCommand = new Command("getvod", "Gets information for a VOD.", new String[]{"~getvod ID"}, true)
        {
            public void Execute(MessageReceivedEvent event, String[] args)
            {
                try
                {
                    VODUtils.SelectVODRecord(Integer.parseInt(args[1]), event);
                }
                catch (NumberFormatException e)
                {
                    BotUtils.SendMessage(event.getChannel(), "VOD ID must be a number.");
                }
            }
        };

        commandMap.put(addVodCommand.commandName, addVodCommand);
        commandMap.put(delVodCommand.commandName, delVodCommand);
        commandMap.put(getVodCommand.commandName, getVodCommand);
    }
}
