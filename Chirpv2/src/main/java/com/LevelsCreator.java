package com;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class LevelsCreator
{
    public static final double MIN_XP_PER_MESSAGE = 15.0;
    public static final double MAX_XP_PER_MESSAGE = 25.0;

    private double[] levelBarriers = new double[502];

    private Map<User, Instant> lastMsg = new HashMap<>();

    Logger logger;

    public LevelsCreator(DiscordClient cli)
    {
        logger = LoggerFactory.getLogger("Levels Logger");
        logger.info("Setting up level utilities...");

        PopulateLevelBarriers();
    }

    public void PopulateLevelBarriers()
    {
        logger.info("Populating level barriers...");
        levelBarriers[0] = 0;
        levelBarriers[1] = 250;
        for (int i = 2; i < 100; i++)
        {
            levelBarriers[i] = Math.floor(levelBarriers[i - 1] + (((4.0 * (i - 1)) / 8.0) * (Math.pow((i - 1), 3.0 / 2.0) ) + 350.0));
        }

        for(int i = 100; i <501; i++)
        {
            levelBarriers[i] = levelBarriers[i-1] + (levelBarriers[99] - levelBarriers[98]);
        }

        levelBarriers[501] = Integer.MAX_VALUE;
    }
}
