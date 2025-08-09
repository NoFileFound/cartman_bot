package org.cartman.discordbot.commands;

public @interface Option {
    /**
     * The option name.
     */
    String name();

    /**
     * The option description.
     */
    String description() default "";

    /**
     * Is it required?
     */
    boolean required() default false;

    /**
     * The type of the option.
     */
    OptionType type() default OptionType.STRING;

    /**
     * Option choices.
     */
    Choice[] choices() default {};

    enum OptionType {
        STRING,
        INTEGER,
        BOOLEAN,
        USER,
        CHANNEL,
        ROLE,
        MENTIONABLE,
        NUMBER,
        ATTACHMENT
    }
}