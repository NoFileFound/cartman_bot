package org.cartman.discordbot.commands.informations;

// Imports
import com.sun.management.OperatingSystemMXBean;
import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.time.Instant;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.cartman.discordbot.commands.SlashCommand;
import org.cartman.discordbot.commands.SlashCommandHandler;

@SlashCommand(
        name="cpuinfo",
        description = "Displays information about physical machine, where bot is hosted.",
        permission = SlashCommand.CommandPermission.DEVELOPER
)
@SuppressWarnings("unused")
public final class Cpuinfo implements SlashCommandHandler {
    @Override
    public void handle(SlashCommandInteractionEvent event) {
        try {
            OperatingSystemMXBean osBean = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
            double cpuLoad = osBean.getCpuLoad() * 100;
            long totalMemory = osBean.getTotalMemorySize();
            long freeMemory = osBean.getFreeMemorySize();
            long usedMemory = totalMemory - freeMemory;
            double ramUsage = ((double) usedMemory / totalMemory) * 100;
            double availableMemory = ((double) freeMemory / totalMemory) * 100;
            DecimalFormat df = new DecimalFormat("#.##");
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("Machine Information")
                    .setTimestamp(Instant.now())
                    .setColor(new Color(0x00ff2a))
                    .addField("CPU Usage:", df.format(cpuLoad) + "%", false)
                    .addField("Memory Usage:", df.format(ramUsage) + "%", false)
                    .addField("Available Memory:", df.format(availableMemory) + "%", false)
                    .addField("RAM Usage:", (usedMemory / 1_000_000) + "MB", false)
                    .addField("API Latency:", df.format(event.getJDA().getGatewayPing()) + "ms", false)
                    .addField("IP:", InetAddress.getLocalHost().getHostAddress(), false).build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("Unknown error")
                    .setDescription("```Unknown error | ERROR #1000```")
                    .setColor(new Color(0x00ff2a))
                    .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                    .build()).queue();
        }
    }
}