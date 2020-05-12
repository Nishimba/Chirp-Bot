package com;

import discord4j.core.spec.EmbedCreateSpec;

import java.util.function.Consumer;

public class EmbedBuilder
{
    EmbedCreateSpec spec;

    public EmbedBuilder()
    {
        spec = new EmbedCreateSpec();
    }

    public Consumer<EmbedCreateSpec> addField(String title, String field, boolean inline)
    {
        return
    }
}
