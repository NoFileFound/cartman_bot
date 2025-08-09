package org.cartman.discordbot.commands;

// Imports
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashCommand {
    /**
     * The command name.
     */
    String name();

    /**
     * The command description.
     */
    String description();

    /**
     * The command is nsfw or not.
     */
    boolean isNsfw() default false;

    /**
     * The command is guild only or not.
     */
    boolean isGuildOnly() default false;

    /**
     * Required permission to run the command.
     */
    CommandPermission permission() default CommandPermission.NONE;

    /**
     * The command's additional options.
     */
    Option[] options() default {};

    enum CommandPermission {
        NONE,
        VERIFIED,
        VERIFIER,
        DEVELOPER
    }
}