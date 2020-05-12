package com;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.util.Permission;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class VODCommands
{
    DiscordClient cli;

    VODCommands(DiscordClient client, HashMap<String, Command> commandMap)
    {
        cli = client;
        InitiateCommands(commandMap);
    }

    public void InitiateCommands(HashMap<String, Command> commandMap)
    {
        Command helpCommand = new Command("help", "A help command showing how to use Chirp.", new String[] {"~help", "~help [command]"}, false)
        {
            @Override
            public void Execute(MessageCreateEvent event, String[] args)
            {
                //If just "~help" is given, the bot will return all commands it knows in the HashMap.
                if(args.length == 1)
                {
                    //alphabetise help
                    Map<String, Command> map = new TreeMap<>(commandMap);

                    //if admin, put the admin commands in the help command
                    for (Map.Entry<String, Command> command : map.entrySet())
                    {
                        if (command.getValue().isAdmin && event.getMember().get().asMember(event.getGuildId().get()).block().getBasePermissions().block().contains(Permission.ADMINISTRATOR))
                        {
                            spec.addField(command.getValue().commandName, command.getValue().description, false);
                        }
                        else if (!command.getValue().isAdmin)
                        {
                            spec.addField(command.getValue().commandName, command.getValue().description, false);
                        }
                    }
                }
                //Otherwise, the bot will return the usage, title and description of the command given in the first argument after the help command itself.
                else if(args.length == 2)
                {
                    //if the command map has the command
                    if (commandMap.containsKey(args[1]))
                    {
                        //append to the embed the name of the command and the description
                        spec.addField(commandMap.get(args[1]).commandName, commandMap.get(args[1]).description, false);

                        //if the usages are not null, write them to the embed
                        if(commandMap.get(args[1]).usages != null)
                        {
                            spec.addField("This is how you can use " + (commandMap.get(args[1]).commandName) + ":", BotUtils.OutputUsage(commandMap.get(args[1])), false);
                        }
                    }
                    else
                    {
                        event.getMessage().getChannel().flatMap(reply -> reply.createMessage("Sorry, but I don't know the command " + args[1] + ".")).block();
                    }
                }
                Consumer<EmbedCreateSpec> template = embedCreateSpec ->
                {
                    embedCreateSpec.setAuthor("Chirp Help", null, null);
                    embedCreateSpec.setTitle("I'll try my best! Here's what I know.");

                }

                event.getMessage().getChannel().flatMap(reply -> reply.createMessage(messageCreateSpec -> messageCreateSpec.set));
            }
        };

        commandMap.put(helpCommand.commandName, helpCommand);
    }
}
