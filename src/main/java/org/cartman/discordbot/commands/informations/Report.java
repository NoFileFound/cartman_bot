package org.cartman.discordbot.commands.informations;

// Imports
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.cartman.Application;
import org.cartman.api.Misc;
import org.cartman.database.DBManager;
import org.cartman.discordbot.commands.Choice;
import org.cartman.discordbot.commands.Option;
import org.cartman.discordbot.commands.SlashCommand;
import org.cartman.discordbot.commands.SlashCommandHandler;
import java.awt.Color;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SlashCommand(
        name = "report",
        description = "Reports a url or phone number.",
        permission = SlashCommand.CommandPermission.VERIFIED,
        options = {
                @Option(
                        name = "type",
                        description = "Type of query",
                        required = true,
                        type = Option.OptionType.STRING,
                        choices = {
                                @Choice(name = "url", argument = "url")
                        }
                ),
                @Option(
                        name = "query",
                        description = "The url or phone number to report. (For phones must be international format)",
                        required = true,
                        type = Option.OptionType.STRING
                ),
                @Option(
                        name = "reason",
                        description = "The reason to report.",
                        required = true,
                        type = Option.OptionType.STRING,
                        choices = {
                                @Choice(name = "Phishing", argument = "phishing"),
                                @Choice(name = "Tech support scam", argument = "tss"),
                                @Choice(name = "Malware", argument = "malware")
                        }
                )
        }
)
@SuppressWarnings("unused")
public final class Report implements SlashCommandHandler {
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        String type = event.getOption("type").getAsString();
        String query = event.getOption("query").getAsString();
        String reason = event.getOption("reason").getAsString();

        event.getHook().sendMessage("Reporting... It could take up to few minutes").queue();
        if (type.equals("url")) {
            String code = Misc.computeHash(query);
            if (DBManager.getReportedUrls().contains(code)) {
                event.getHook().editOriginalEmbeds(new EmbedBuilder()
                        .setTitle("The url is already been reported..")
                        .setDescription("```Already Reported | ERROR #1010```")
                        .setColor(new Color(0x00ff2a))
                        .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                        .build()).queue();
                return;
            }

            boolean result = Misc.reportUrl(query, reason);
            if (!result) {
                event.getHook().editOriginalEmbeds(new EmbedBuilder()
                        .setTitle("Unable to report the url.")
                        .setDescription("```Timeout | ERROR #1009```")
                        .setColor(new Color(0x00ff2a))
                        .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                        .build()).queue();
                return;
            } else {
                TextChannel channel = event.getJDA().getTextChannelById(Application.getConfigParser().getLong("discordbot.verchannel"));
                if (channel == null) {
                    event.getHook().editOriginalEmbeds(new EmbedBuilder()
                            .setTitle("Invalid verification channel.")
                            .setDescription("```Invalid Verification Channel | ERROR #1006```")
                            .setColor(new Color(0x00ff2a))
                            .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                            .build()).queue();
                    return;
                }

                try {
                    List<String> lines = Files.readAllLines(Path.of("data/" + code + "/info.txt"));
                    String _link = "";
                    String _type = "";
                    String _phone = "";
                    String _country = "";
                    String _hash = "";
                    String _google = "";
                    for (String line : lines) {
                        if (line.startsWith("Link -> ")) {
                            _link = line.substring("Link -> ".length()).trim();
                        } else if (line.startsWith("Type -> ")) {
                            _type = line.substring("Type -> ".length()).trim();
                        } else if (line.startsWith("Phone -> ")) {
                            _phone = line.substring("Phone -> ".length()).trim();
                        } else if (line.startsWith("Country -> ")) {
                            _country = line.substring("Country -> ".length()).trim();
                        } else if (line.startsWith("Hash -> ")) {
                            _hash = line.substring("Hash -> ".length()).trim();
                        } else if (line.startsWith("Google Safebrowsing -> ")) {
                            _google = line.substring("Google Safebrowsing -> ".length()).trim();
                        }
                    }

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("🚨 Report Request");
                    embed.setDescription("A user has submitted a report. Please review below:");
                    embed.setColor(Color.ORANGE);
                    embed.addField("Link", _link, false);
                    embed.addField("Type", _type, false);
                    if (_type.equals("Tech Support Scam (Phishing)")) {
                        embed.addField("Phone", _phone, false);
                        embed.addField("Country", _country, false);
                    }
                    embed.addField("Hash", _hash, false);
                    embed.addField("Google safebrowsing", _google, false);
                    embed.setThumbnail("https://img.icons8.com/color/344/list--v1.png");
                    embed.setAuthor(event.getUser().getName());
                    channel.sendMessageEmbeds(embed.build()).addActionRow(
                            Button.success("btn_accept_report|" + event.getUser().getId() + "|" + code, "Accept"),
                            Button.danger("btn_reject_report|" + event.getUser().getId() + "|" + code, "Reject")).addFiles(FileUpload.fromData(new File("data/" + code + "/screenshot.png"))).queue();
                } catch (Exception _) {

                }
            }
            event.getHook().sendMessage("The report was submitted successfully.").queue();
        }
    }
}