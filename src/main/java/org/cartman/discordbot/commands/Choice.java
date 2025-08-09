package org.cartman.discordbot.commands;

public @interface Choice {
    /**
     * The choice name.
     */
    String name();

    /**
     * The choice argument.
     */
    String argument();
}