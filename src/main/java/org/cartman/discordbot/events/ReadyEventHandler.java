package org.cartman.discordbot.events;

// Imports
import java.util.Date;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public final class ReadyEventHandler extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();

        /// TODO: FIX

        jda.getPresence().setActivity(Activity.playing(String.format("Online at %s", new Date())));
        jda.getPresence().setStatus(net.dv8tion.jda.api.OnlineStatus.ONLINE);
    }
}