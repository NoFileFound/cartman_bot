package org.cartman.discordbot.events;

// Imports
import java.awt.Color;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.cartman.api.DomainLookup;
import org.cartman.api.GoogleSafebrowsingLookup;
import org.cartman.api.Playit;
import org.cartman.api.SMTP;
//import org.cartman.api.Scammerinfo;
import org.cartman.api.Urlscanio;
import org.cartman.database.DBManager;
import org.cartman.database.DBUtils;
import org.cartman.database.collections.User;
import org.jetbrains.annotations.NotNull;

public final class ButtonInteractionEventHandler extends ListenerAdapter {
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        String componentId = event.getComponentId();
        String[] parts = componentId.split("\\|");

        if(DBManager.getUsers().get(event.getUser().getIdLong()) == null || DBManager.getUsers().get(event.getUser().getIdLong()).getPrivilegeId() < 1) {
            return;
        }

        if(parts[0].equals("btn_req_accept") || parts[0].equals("btn_req_reject")) {
            if(parts.length != 3) return;

            long _userId = Long.parseLong(parts[1]);
            long _guildId = Long.parseLong(parts[2]);
            boolean isAccepted = parts[0].equals("btn_req_accept");
            if(isAccepted) {
                DBUtils.addUserObject(_userId, _guildId, event.getJDA().getGuildById(_guildId).getOwnerIdLong());
                event.getJDA().retrieveUserById(_userId).queue(user -> user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Congrats, You are now registered in our system.").queue()));
            } else {
                event.getJDA().retrieveUserById(_userId).queue(user -> user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Unfortunately you are rejected from being registered.").queue()));
            }

            EmbedBuilder updatedEmbed = new EmbedBuilder(event.getMessage().getEmbeds().getFirst());
            updatedEmbed.addField("Status", isAccepted ? "✅ Accepted" : "❌ Rejected", false);
            updatedEmbed.setColor(isAccepted ? Color.GREEN : Color.RED);
            event.getHook().editOriginalEmbeds(updatedEmbed.build())
                    .setComponents(ActionRow.of(
                            Button.success("btn_req_accept|" + _userId + "|" + _guildId, "Accept").asDisabled(),
                            Button.danger("btn_req_reject|" + _userId + "|" + _guildId, "Reject").asDisabled()
                    )).queue();
        } else if(parts[0].equals("btn_accept_report") || parts[0].equals("btn_reject_report")) {
            if(parts.length != 3) return;

            long _userId = Long.parseLong(parts[1]);
            String code = parts[2];
            boolean isAccepted = parts[0].equals("btn_accept_report");
            if(isAccepted) {
                User myUser = DBUtils.findUserObjectById(_userId);
                myUser.setKarma(myUser.getKarma() + 1);
                myUser.save();


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

                    String whoisData = DomainLookup.getWhoisInfo(new URI(_link).getHost().replaceFirst("^www\\.", ""));
                    String abuseEmail;
                    String lower = whoisData.toLowerCase();
                    int ai = lower.indexOf("abuse");
                    if (ai == -1) abuseEmail =  "abuse email not found";
                    int at = whoisData.indexOf('@', ai);
                    if (at == -1) abuseEmail =  "abuse email not found";
                    int start = whoisData.lastIndexOf(' ', at) + 1;
                    int end = whoisData.indexOf(' ', at);
                    if (end == -1) end = whoisData.length();
                    abuseEmail = whoisData.substring(start, end).trim();

                    Urlscanio.scanUrl(_link);
                    GoogleSafebrowsingLookup.reportUrl(_type.contains("malware") ? "MALWARE" : "SOCIAL_ENGINEERING", _link);
                    if(!abuseEmail.equals("abuse email not found") && !abuseEmail.equals("notfound")) {
                        SMTP.sendEmail(abuseEmail, "Phishing url", _link, false);
                    }

                    if(_link.contains(".playit.gg")) {
                        Playit.reportEndpoint("TCP", _link);
                    }

                    if(_type.contains("Tech")) {
                        //Scammerinfo.createTopic("Tech Support Scam -> " + _phone, String.format("Link -> %s\nType -> %s\nPhone -> %s\nCountry -> %s\nHash -> %s\nGoogle safebrowsing -> %s\nScreenshot:", _link, _type, _phone, _country, _hash, _google));
                    }
                } catch (Exception _) {

                }
            }

            EmbedBuilder updatedEmbed = new EmbedBuilder(event.getMessage().getEmbeds().getFirst());
            updatedEmbed.addField("Status", isAccepted ? "✅ Accepted" : "❌ Rejected", false);
            updatedEmbed.setColor(isAccepted ? Color.GREEN : Color.RED);
            event.getHook().editOriginalEmbeds(updatedEmbed.build())
                    .setComponents(ActionRow.of(
                            Button.success("btn_accept_report|"+_userId+"|"+code, "Accept").asDisabled(),
                            Button.danger("btn_reject_report|"+_userId+"|"+code, "Reject").asDisabled())
                    ).queue();
        }
    }
}