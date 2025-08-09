package org.cartman.discordbot.commands;

// Imports
import java.awt.Color;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.cartman.Application;
import org.cartman.database.DBManager;
import org.cartman.database.collections.User;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

public final class CommandLoader extends ListenerAdapter {
    @Getter private final Map<String, SlashCommand> annotations;
    private final Map<String, SlashCommandHandler> commands;

    public CommandLoader() {
        this.commands = new TreeMap<>();
        this.annotations = new TreeMap<>();

        // registers all commands.
        this.loadCommands();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        String commandName = event.getName();
        SlashCommand annotation = this.annotations.get(commandName);
        if(annotation == null) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("Unknown error")
                    .setDescription("```Unknown error | ERROR #1000```")
                    .setColor(new Color(0x00ff2a))
                    .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                    .build()).queue();
            return;
        }

        if (!event.isFromGuild() && annotation.isGuildOnly()) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("This command is available only in servers.")
                    .setDescription("```Not Guild Command | ERROR #1001```")
                    .setColor(new Color(0x00ff2a))
                    .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                    .build()).queue();
            return;
        }

        if(event.isFromGuild() && !event.getChannel().asTextChannel().isNSFW() && annotation.isNsfw()) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("You can't run NSFW commands outside of an NSFW channel.")
                    .setDescription("```Not NSFW Channel | ERROR #1002```")
                    .setColor(new Color(0x00ff2a))
                    .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                    .build()).queue();
        }

        if(!this.checkPermission(event.getUser().getIdLong(), annotation.permission())) {
            event.getHook().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("You don't have permission to execute this command.")
                    .setDescription("```Not enough permissions | ERROR #1003```")
                    .setColor(new Color(0x00ff2a))
                    .setThumbnail("https://img.icons8.com/color/344/minus--v1.png")
                    .build()).queue();
            return;
        }

        SlashCommandHandler handler = this.commands.get(event.getName());
        if (handler != null) {
            handler.handle(event);
        }
    }

    /**
     * Checks if given user can execute the command.
     * @param userId The given user id.
     * @param permission Command permission.
     * @return True if he can run the command or else false.
     */
    private boolean checkPermission(long userId, SlashCommand.CommandPermission permission) {
        if(permission == SlashCommand.CommandPermission.NONE) return true;

        User userObj = DBManager.getUsers().get(userId);
        if(userObj == null || userObj.getPrivilegeId() == -1) return false;

        return switch (permission) {
            case VERIFIED -> userObj.getPrivilegeId() >= 0;
            case VERIFIER  -> userObj.getPrivilegeId() >= 1;
            case DEVELOPER -> userObj.getPrivilegeId() == 2;
            default -> false;
        };
    }

    /**
     * Registers all commands.
     */
    private void loadCommands() {
        Reflections reflector = Application.getReflector();
        Set<Class<?>> classes = reflector.getTypesAnnotatedWith(SlashCommand.class);
        AtomicInteger cnt = new AtomicInteger();

        classes.forEach(
                annotated -> {
                    try {
                        SlashCommand cmdData = annotated.getAnnotation(SlashCommand.class);
                        Object object = annotated.getDeclaredConstructor().newInstance();
                        if (object instanceof SlashCommandHandler) {
                            cnt.getAndIncrement();
                            SlashCommand annotation = object.getClass().getAnnotation(SlashCommand.class);
                            this.annotations.put(cmdData.name().toLowerCase(), annotation);
                            this.commands.put(cmdData.name().toLowerCase(), (SlashCommandHandler)object);
                        }
                    } catch (Exception ignored) {
                        Application.getLogger().error(String.format("Unable to register the command: %s", annotated.getSimpleName()));
                    }
                }
        );
        Application.getLogger().info("Registered commands: {}", cnt.get());
    }
}