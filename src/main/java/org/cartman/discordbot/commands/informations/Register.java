package org.cartman.discordbot.commands.informations;

// Imports
import java.awt.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.cartman.Application;
import org.cartman.database.DBManager;
import org.cartman.discordbot.commands.SlashCommand;
import org.cartman.discordbot.commands.SlashCommandHandler;

@SlashCommand(
        name="register",
        description = "Registers you in the system.",
        permission = SlashCommand.CommandPermission.NONE,
        isGuildOnly = true
)
@SuppressWarnings("unused")
public final class Register implements SlashCommandHandler {
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        if(DBManager.getUsers().containsKey(userId)) {
            int privLevel = DBManager.getUsers().get(userId).getPrivilegeId();
            if(privLevel > -1) {
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("You are already registered in the system.")
                        .setDescription("```Already Registered | ERROR #1004```")
                        .setColor(new Color(0x00ff2a))
                        .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                        .build()).queue();
                return;
            }

            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("You are blacklisted from using this bot.")
                    .setDescription("```User In Blacklist | ERROR #1005```")
                    .setColor(new Color(0x00ff2a))
                    .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                    .build()).queue();
            return;
        }

        TextChannel channel = event.getJDA().getTextChannelById(Application.getConfigParser().getLong("discordbot.verchannel"));
        if(channel == null) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("Invalid verification channel.")
                    .setDescription("```Invalid Verification Channel | ERROR #1006```")
                    .setColor(new Color(0x00ff2a))
                    .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                    .build()).queue();
            return;
        }

        event.getHook().sendMessage("Your request was submitted successfully. When we check you will receive a DM from us.").queue(response -> channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle("🚨 Review Request")
                .setDescription("A user has submitted a request. Please review below:")
                .addField("👤 User", event.getUser().getName() + " (`" + event.getUser().getId() + "`)", false)
                .addField("🌐 Guild", event.getGuild().getName() + " (`" + event.getGuild().getId() + "`)", false)
                .setColor(Color.ORANGE)
                .setThumbnail("https://img.icons8.com/color/344/list--v1.png")
                .setFooter("Would you like to give this user to access bunch of private tools?").build())
                .addActionRow(
                    Button.success("btn_req_accept|"+event.getUser().getId()+"|"+event.getGuild().getId(), "Accept"),
                    Button.danger("btn_req_reject|"+event.getUser().getId()+"|"+event.getGuild().getId(), "Reject"))
                .queue());
    }
}