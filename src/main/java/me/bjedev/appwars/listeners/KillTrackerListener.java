package me.bjedev.appwars.listeners;

import me.bjedev.appwars.AppWars;
import me.bjedev.appwars.listeners.custom.PlayerKillEvent;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.UUID;

/**
 * Thanks Fluyd
 */
public final class KillTrackerListener implements Listener {
    private final HashMap<UUID, UUID> lastDamagers = new HashMap<>();

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                lastDamagers.put(victim.getUniqueId(), damager.getUniqueId());
            } else if (event.getDamager() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getDamager();
                if (arrow.getShooter() instanceof Player) {
                    Player shooter = (Player) arrow.getShooter();
                    lastDamagers.put(victim.getUniqueId(), shooter.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (lastDamagers.containsKey(victim.getUniqueId())) {
            UUID killerUUID = lastDamagers.remove(victim.getUniqueId());
            if (killerUUID != null) {
                Player killer = AppWars.INSTANCE.getServer().getPlayer(killerUUID);
                if (killer != null) {
                    PlayerKillEvent killEvent = new PlayerKillEvent(killer, victim);
                    AppWars.INSTANCE.getServer().getPluginManager().callEvent(killEvent);
                }
            }
        }
    }
}