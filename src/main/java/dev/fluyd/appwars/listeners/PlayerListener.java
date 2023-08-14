package dev.fluyd.appwars.listeners;

import dev.fluyd.appwars.game.Countdown;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.utils.GameState;
import dev.fluyd.appwars.utils.ScoreboardHandler;
import dev.fluyd.appwars.utils.config.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final int players = Bukkit.getOnlinePlayers().size();

        final int minPlayers = ConfigUtils.INSTANCE.minPlayers;
        final int maxPlayers = ConfigUtils.INSTANCE.maxPlayers;

        final Player p = e.getPlayer();

        if (players >= maxPlayers) {
            e.setJoinMessage(null);
            p.kickPlayer(ChatColor.RED + "Game is full :(");
            return;
        }

        e.setJoinMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7(&b%s&7/&b%s&7)&e %s joined.", players, maxPlayers, p.getName())));
        new ScoreboardHandler(p, "§eAPP WARS");

        if (ConfigUtils.INSTANCE.lobbyLocation != null && GameManager.state != GameState.STARTED)
            e.getPlayer().teleport(ConfigUtils.INSTANCE.lobbyLocation);

        if (players >= minPlayers && GameManager.state != GameState.STARTED) {
            final Countdown countdown = new Countdown(5);
            countdown.start(GameManager::start);
        }
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        final Entity entity = e.getEntity();
        if (!(entity instanceof Player))
            return;

        final Player p = (Player) entity;

        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);
            return;
        }

        if (GameManager.state != GameState.STARTED) {
            e.setCancelled(true);
            return;
        }

        final UUID uuid = p.getUniqueId();
        if (!GameManager.players.containsKey(uuid)) {
            e.setCancelled(true);
            return;
        }

        final Arena arena = GameManager.players.get(uuid);
        if (arena != null && !arena.isPvp())
            e.setCancelled(true);
    }

    @EventHandler
    public void onDropItem(final PlayerDropItemEvent e) {
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();

        if (!GameManager.players.containsKey(uuid))
            return;

        final Arena arena = GameManager.players.get(uuid);
        if (arena != null && !arena.isDropItems())
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();

        if (!GameManager.players.containsKey(uuid))
            return;

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final Arena arena = GameManager.players.get(uuid);
            if (arena != null && !arena.isInteract())
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) {
        e.setDeathMessage(null);
        e.getDrops().clear();
        e.setDroppedExp(0);
    }

    private void dropAndClearInventory(Player player, Location dropLocation) {
        PlayerInventory inventory = player.getInventory(); // Player's inventory

        // Loop through items and drop them
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) { // Make sure the item slot isn't empty
                player.getWorld().dropItemNaturally(dropLocation, item);
            }
        }

        // Clear the inventory
        inventory.clear();
    }
}