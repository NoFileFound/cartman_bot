package org.cartman.discordbot.commands.informations;

// Imports
import java.awt.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.cartman.api.DomainLookup;
import org.cartman.api.IPLookup;
import org.cartman.api.MobileLookup;
import org.cartman.discordbot.commands.Choice;
import org.cartman.discordbot.commands.Option;
import org.cartman.discordbot.commands.SlashCommand;
import org.cartman.discordbot.commands.SlashCommandHandler;

@SlashCommand(
        name = "whois",
        description = "Lookup info by domain, IP or phone number.",
        permission = SlashCommand.CommandPermission.VERIFIED,
        options = {
                @Option(
                        name = "type",
                        description = "Type of query",
                        required = true,
                        type = Option.OptionType.STRING,
                        choices = {
                                @Choice(name = "domain", argument = "domain"),
                                @Choice(name = "ip", argument = "ip"),
                                @Choice(name = "phone", argument = "phone")
                        }
                ),
                @Option(
                        name = "query",
                        description = "The domain, IP or phone number to lookup.",
                        required = true,
                        type = Option.OptionType.STRING
                )
        }
)
@SuppressWarnings("unused")
public final class Whois implements SlashCommandHandler {
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String type = event.getOption("type").getAsString();
        String query = event.getOption("query").getAsString();
        String information = switch (type) {
            case "domain" -> DomainLookup.getWhoisInfo(query);
            case "ip" -> IPLookup.getIPAddressInfo(query);
            case "phone" -> MobileLookup.getPhoneNumberInfo(query);
            default -> "";
        };

        switch (information) {
            case "unknown" -> {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Unknown error.")
                        .setDescription("```Unknown error | ERROR #1000```")
                        .setColor(new Color(0x00ff2a))
                        .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                        .build()).queue();
                return;
            }
            case "invalidkey" -> {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Invalid API key.")
                        .setDescription("```Invalid API Key | ERROR #1007```")
                        .setColor(new Color(0x00ff2a))
                        .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                        .build()).queue();
                return;
            }
            case "invalidip" -> {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Invalid IP Address.")
                        .setDescription("```Invalid Query | ERROR #1008```")
                        .setColor(new Color(0x00ff2a))
                        .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                        .build()).queue();
                return;
            }
            case "reservedip" -> {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("The IP Address is reserved.")
                        .setDescription("```Invalid Query | ERROR #1008```")
                        .setColor(new Color(0x00ff2a))
                        .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                        .build()).queue();
                return;
            }
            case "notfound" -> {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Domain not found.")
                        .setDescription("```Not Found | ERROR #1009```")
                        .setColor(new Color(0x00ff2a))
                        .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                        .build()).queue();
                return;
            }
        }

        event.getHook().sendMessage("```" + information + "```").queue();
    }
}