package org.cartman;

// Imports
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.cartman.api.Proxy;
import org.cartman.api.SMTP;
import org.cartman.config.ConfigParser;
import org.cartman.database.DBManager;
import org.cartman.discordbot.commands.Choice;
import org.cartman.discordbot.commands.CommandLoader;
import org.cartman.discordbot.commands.Option;
import org.cartman.discordbot.events.ButtonInteractionEventHandler;
import org.cartman.discordbot.events.ReadyEventHandler;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    @Getter private static final ConfigParser configParser = new ConfigParser();
    @Getter private static final Logger logger = LoggerFactory.getLogger(Application.class);
    @Getter private static final Reflections reflector = new Reflections("org.cartman");
    private static final CommandLoader commandHandler = new CommandLoader();

    public static void main(String[] args) throws Exception {
        DBManager.initializeDatabase();
        SMTP.initSMTP();
        Proxy.reloadProxies();
        JDA jda = JDABuilder.createDefault(configParser.getString("discordbot.token")).build();
        jda.addEventListener(new ReadyEventHandler());
        jda.addEventListener(commandHandler);
        jda.addEventListener(new ButtonInteractionEventHandler());
        jda.awaitReady();
        var commandData = commandHandler.getAnnotations().values().stream()
                .map(cmd -> {
                    var slash = Commands.slash(cmd.name(), cmd.description()).setNSFW(cmd.isNsfw()).setGuildOnly(cmd.isGuildOnly());
                    for (Option opt : cmd.options()) {
                        OptionData optionData = new OptionData(
                                switch (opt.type()) {
                                    case STRING -> OptionType.STRING;
                                    case INTEGER -> OptionType.INTEGER;
                                    case BOOLEAN -> OptionType.BOOLEAN;
                                    case USER -> OptionType.USER;
                                    case CHANNEL -> OptionType.CHANNEL;
                                    case ROLE -> OptionType.ROLE;
                                    case MENTIONABLE -> OptionType.MENTIONABLE;
                                    case NUMBER -> OptionType.NUMBER;
                                    case ATTACHMENT -> OptionType.ATTACHMENT;
                                },
                                opt.name(),
                                opt.description(),
                                opt.required()
                        );

                        for (Choice choice : opt.choices()) {
                            optionData.addChoice(choice.name(), choice.argument());
                        }
                        slash.addOptions(optionData);
                    }
                    return slash;
                })
                .toList();

        jda.updateCommands().addCommands(commandData).queue();
    }
}