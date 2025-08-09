package org.cartman.discordbot.commands.admin;

// Imports
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.cartman.api.Proxy;
import org.cartman.discordbot.commands.SlashCommand;
import org.cartman.discordbot.commands.SlashCommandHandler;

@SlashCommand(name="refreshproxies", description = "Refreshes the proxy list.", permission = SlashCommand.CommandPermission.VERIFIER)
@SuppressWarnings("unused")
public final class Refreshproxies implements SlashCommandHandler {
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        try {
            int proxies = Proxy.reloadProxies();
            event.getHook().sendMessage("Fetched total: **" + proxies + "** proxies.").queue();
        } catch (Exception e) {
            event.getHook().sendMessage("Failed to fetch proxies: " + e.getMessage()).queue();
        }
    }
}