package org.cartman.discordbot.commands.admin;

// Imports
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.cartman.discordbot.commands.SlashCommand;
import org.cartman.discordbot.commands.SlashCommandHandler;

@SlashCommand(name="shutdown", description = "Shutdowns the bot.", permission = SlashCommand.CommandPermission.DEVELOPER)
@SuppressWarnings("unused")
public final class Shutdown implements SlashCommandHandler {
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage("Performing a shutdown...").queue(response -> event.getJDA().shutdown());
    }
}