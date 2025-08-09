package org.cartman.discordbot.commands;

// Imports
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SlashCommandHandler {
    /**
     * Handles the current command.
     * @param event The command's event.
     */
    void handle(SlashCommandInteractionEvent event);
}