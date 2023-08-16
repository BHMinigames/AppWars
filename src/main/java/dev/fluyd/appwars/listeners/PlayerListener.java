package dev.fluyd.appwars.listeners;

import dev.fluyd.appwars.game.Countdown;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.listeners.custom.PlayerKillEvent;
import dev.fluyd.appwars.mirror.Mirror;
import dev.fluyd.appwars.utils.GameState;
import dev.fluyd.appwars.utils.ScoreboardHandler;
import dev.fluyd.appwars.utils.config.ConfigUtils;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.CraftingInventory;
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

        // REMOVE AFTER TESTING
        if (!GameManager.playerMirrors.containsKey(p))
            GameManager.playerMirrors.put(p, new Mirror(p));
        // REMOVE AFTER TESTING

        if (players >= maxPlayers) {
            e.setJoinMessage(null);
            p.kickPlayer(ChatColor.RED + "Game is full :(");
            return;
        }

        final UUID uuid = p.getUniqueId();
        if (GameManager.state == GameState.STARTED && !GameManager.players.containsKey(uuid)) { // Prevent things like players leaving in one round and joining back later and still be in the previous place
            e.setJoinMessage(null);
            p.kickPlayer(ChatColor.RED + "This game has already started.");
            return;
        }

        if (GameManager.state == GameState.STARTED)
            e.setJoinMessage(ChatColor.translateAlternateColorCodes('&', String.format("&e%s joined back.", p.getName())));
        else
            e.setJoinMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7(&b%s&7/&b%s&7)&e %s joined.", players, maxPlayers, p.getName())));
        new ScoreboardHandler(p, "§eAPP WARS");

        if (ConfigUtils.INSTANCE.lobbyLocation != null && GameManager.state != GameState.STARTED)
            GameManager.reset(p);

        if (players >= minPlayers && GameManager.state != GameState.STARTED) {
            final Countdown countdown = new Countdown(5);
            countdown.start(GameManager::start);
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();

        Mirror playerMirror = GameManager.playerMirrors.get(e.getPlayer());

        if (playerMirror != null)
            playerMirror.deleteNPC();

        if (GameManager.state == GameState.STARTED && !GameManager.players.containsKey(uuid))
            e.setQuitMessage(null);
    }

    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        final Entity entity = e.getEntity();
        if (!(entity instanceof Player))
            return;

        final Player p = (Player) entity;
        final UUID uuid = p.getUniqueId();

        final EntityDamageEvent.DamageCause cause = e.getCause();

        if (cause == EntityDamageEvent.DamageCause.FALL) {
            if (GameManager.state != GameState.STARTED) {
                e.setCancelled(true);
                return;
            }

            if (GameManager.players.containsKey(uuid)) {
                final Arena arena = GameManager.players.get(uuid);
                if (arena.isNoFall()) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (GameManager.state != GameState.STARTED) {
            e.setCancelled(true);
            return;
        }

        if (!GameManager.players.containsKey(uuid)) {
            e.setCancelled(true);
            return;
        }

        final Arena arena = GameManager.players.get(uuid);

        if (cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION || cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            if (arena != null && !arena.isPvp())
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
        final Entity entity = e.getEntity();
        final Entity damager = e.getDamager();

        if (!(entity instanceof Player) || !(damager instanceof Player))
            return;

        final Player hitter = (Player) damager;
        final UUID uuid = hitter.getUniqueId();

        if (!GameManager.players.containsKey(uuid))
            return;

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
    public void onClickInventory(final InventoryClickEvent e) {
        if (e.getSlotType() == InventoryType.SlotType.CRAFTING)
            e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
//        e.getRawSlots().forEach(System.out::println);

        if (e.getInventory() instanceof CraftingInventory) {
            for (int slot : e.getRawSlots()) {
                if (slot > 0 && slot < 5) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();
        Mirror playerMirror = GameManager.playerMirrors.get(e.getPlayer());

        if (playerMirror != null && e.getAction().toString().contains("LEFT"))
            playerMirror.makeNpcSwingArm();


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

    @EventHandler
    public void onPlayerKill(final PlayerKillEvent e) {
        final Player player = e.getVictim();
        final Location deathLocation = e.getKiller().getLocation();

        dropAndClearInventory(player, deathLocation);

        player.spigot().respawn();
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(e.getKiller().getLocation());

        GameManager.eliminated(player, "&eYou died!");

        final Player killer = e.getKiller();
        GameManager.victory(killer, "&eYou won the duel!");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Mirror playerMirror = GameManager.playerMirrors.get(e.getPlayer());

        if (playerMirror != null)
            playerMirror.handleReflect();

        if (!GameManager.playerMirrors.containsKey(e.getPlayer()) && playerMirror == null && Mirror.isInMirrorBox(e.getPlayer()))
            GameManager.playerMirrors.put(e.getPlayer(), new Mirror(e.getPlayer()));

        if (GameManager.playerMirrors.containsKey(e.getPlayer()) && playerMirror != null && !playerMirror.isInMirrorBox() && playerMirror.npc != null)
            playerMirror.deleteNPC();
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        Mirror playerMirror = GameManager.playerMirrors.get(e.getPlayer());

        if (playerMirror != null) {
            playerMirror.setNpcSneaking(e.isSneaking());
        }
    }

//    @EventHandler
//    public void onItemChange(PlayerItemHeldEvent e) {
//        Player player = e.getPlayer();
//        ItemStack newItem = player.getInventory().getItem(e.getNewSlot());
//
//        Mirror playerMirror = GameManager.playerMirrors.get(e.getPlayer());
//
//        if (playerMirror != null && playerMirror.npc != null)
//            playerMirror.copyItemToNPC(newItem);
//    }
//
//    @EventHandler
//    public void onInventoryPickupItem(InventoryPickupItemEvent e) {
//        Player player = (Player) e.getInventory().getHolder();
//        ItemStack newItem = player.getItemInHand();
//
//        Mirror playerMirror = GameManager.playerMirrors.get(player);
//
//        if (playerMirror != null && playerMirror.npc != null)
//            playerMirror.copyItemToNPC(newItem);
//    }
//
//    @EventHandler
//    public void onInventoryClick(InventoryClickEvent e) {
//        Player player = (Player) e.getInventory().getHolder();
//        ItemStack newItem = player.getItemInHand();
//
//        Mirror playerMirror = GameManager.playerMirrors.get(player);
//
//        if (playerMirror != null && playerMirror.npc != null)
//            playerMirror.copyItemToNPC(newItem);
//    }

    private void dropAndClearInventory(final Player player, final Location dropLocation) {
        final PlayerInventory inventory = player.getInventory();

        for (final ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR)
                player.getWorld().dropItemNaturally(dropLocation, item);
        }

        GameManager.clearInv(player);
    }
}