package com;

import discord4j.core.DiscordClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelsCreator
{
    private double[] levelBarriers = new double[502];

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
