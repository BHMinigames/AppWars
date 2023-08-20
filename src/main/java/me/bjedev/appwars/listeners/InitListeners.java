package me.bjedev.appwars.listeners;

import me.bjedev.appwars.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class InitListeners implements Listener {
    public Runnable onWorldLoad = null;

    @EventHandler
    public void onWorldLoad(final WorldLoadEvent e) {
        if (onWorldLoad != null) {
            onWorldLoad.run();
            onWorldLoad = null;
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        if (GameManager.arenas.isEmpty()) {
            e.setJoinMessage(null);
            p.kickPlayer(ChatColor.RED + "Game is not set up yet...");
        }
    }
}