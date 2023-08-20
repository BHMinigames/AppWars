package dev.fluyd.appwars.game.arena.impl;

import dev.fluyd.appwars.AppWars;
import dev.fluyd.appwars.game.arena.AboutArena;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.game.arena.MagicFloor;
import dev.fluyd.appwars.game.arena.WoolColors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@AboutArena(name = "PAINT", pvp = true, damage = false, allowInteraction = true, subTitle = "&eCover as much land with your wool color as possible.")
public final class Paint extends Arena implements Listener {
    private final Map<UUID, Player> eggThrowMap = new HashMap<>();
    private final Map<UUID, WoolColors> woolColorMap = new HashMap<>();
    private MagicFloor floor;
    private BukkitTask eggTask;

    @Override
    public void start() throws Exception {
        if (super.getMagicFloors().isEmpty())
            throw new Exception("No floor set");

        this.floor = super.getMagicFloors().get(0); // Gets the first magic floor this arena has
        this.resetFloor();
        this.setWoolColors();

        super.teleport();
        super.sendTitle();

        this.startDroppingEggs();
    }

    private void startDroppingEggs() {
        this.eggTask = Bukkit.getScheduler().runTaskTimer(AppWars.INSTANCE, () -> {
            if (new Random().nextInt(4) + 1 == 1) // 1/4 of the time
                return;

            for (final Player p : this.getPlayers()) {
                final Location loc = p.getLocation();
                final List<Block> circle = this.floor.circle(loc, 5);

                final Block block = circle.get(new Random().nextInt(circle.size()));
                final Location toDropAt = block.getLocation().add(0, 2, 0);

                if (!new Random().nextBoolean())
                    continue;

                toDropAt.getWorld().dropItem(toDropAt, new ItemStack(Material.EGG, 1));
            }
        }, 40L, 40L);
    }

    private void setWoolColors() {
        final WoolColors color1 = WoolColors.getRandomColor();
        final WoolColors color2 = WoolColors.getRandomColor(color1);

        this.woolColorMap.put(super.getUuid1(), color1);
        this.woolColorMap.put(super.getUuid2(), color2);
    }

    @Override
    public void reset() {
        this.resetFloor();

        this.floor = null;
        this.eggThrowMap.clear();
        this.woolColorMap.clear();

        this.eggTask.cancel();
        this.eggTask = null;
    }

    private void resetFloor() {
        if (this.floor == null)
            return;

        this.floor.getBlocks().forEach(block -> block.setType(Material.WOOL));
    }

    @Override
    public void enable(final JavaPlugin instance) {
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        final Entity entity = e.getEntity();
        if (!(entity instanceof Player))
            return;

        final Player p = (Player) entity;
        final UUID uuid = p.getUniqueId();

        if (!this.containsUuid(uuid))
            return;

        final Location loc = p.getLocation();
        this.drawCircle(loc, 3, uuid);
    }

    private void drawCircle(final Location center, final int size, final UUID uuid) {
        final List<Block> circle = this.floor.circle(center, 3);

        final WoolColors color = this.woolColorMap.get(uuid);
        circle.forEach(block -> {
            block.setType(Material.WOOL);
            block.setData(color.getLegacyColorId());
        });

        this.pop(center);
    }

    @EventHandler
    public void onPlayerEggThrow(final PlayerEggThrowEvent e) {
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();
        final Egg egg = e.getEgg();

        if (!super.containsUuid(uuid))
            return;

        this.eggThrowMap.put(egg.getUniqueId(), p);
    }

    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent e) {
        final Entity entity = e.getEntity();

        if (!(entity instanceof Egg))
            return;

        final Egg egg = (Egg) entity;
        final UUID uuid = egg.getUniqueId();

        if (!this.eggThrowMap.containsKey(uuid))
            return;

        final Player p = this.eggThrowMap.get(uuid);
        final Location loc = p.getLocation();

        this.drawCircle(loc, 4, p.getUniqueId());
    }

    /**
     * To be called when a player is hit / egg lands
     * @param loc
     */
    private void pop(final Location loc) {
        loc.getWorld().playSound(loc, Sound.CHICKEN_EGG_POP, 1F, 1F);
    }
}