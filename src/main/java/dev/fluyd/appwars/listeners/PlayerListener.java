package dev.fluyd.appwars.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);

        e.getPlayer().teleport(new Location(e.getPlayer().getWorld(), 22.5, 128, 73.5));

        e.getPlayer().teleport(e.getPlayer().getLocation().setDirection(new Location(e.getPlayer().getWorld(), 22.5, 128, 72.5).toVector().subtract(e.getPlayer().getLocation().toVector())));
    }
}
