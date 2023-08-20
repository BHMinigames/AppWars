package me.bjedev.appwars.game.arena.impl;

import me.bjedev.appwars.AppWars;
import me.bjedev.appwars.game.arena.AboutArena;
import me.bjedev.appwars.game.arena.Arena;
import me.bjedev.appwars.game.arena.MagicFloor;
import me.bjedev.appwars.game.arena.WoolColors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@AboutArena(name = "PAINT", pvp = true, damage = false, allowInteraction = true, subTitle = "&eCover as much land with your wool color as possible.")
public final class Paint extends Arena implements Listener {
    private final Map<UUID, Player> eggThrowMap = new HashMap<>();
    private final Map<UUID, WoolColors> woolColorMap = new HashMap<>();
    private final Map<UUID, List<Location>> woolCountMap = new HashMap<>();
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

        if (this.eggTask == null)
            return;

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
    public void onEntityDamage(final EntityDamageByEntityEvent e) {
        final Entity damagerEntity = e.getDamager();
        final Entity victimEntity = e.getEntity();
        if (!(damagerEntity instanceof Player))
            return;

        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        final Player p = (Player) damagerEntity;
        final UUID uuid = p.getUniqueId();

        if (!this.containsUuid(uuid))
            return;

        final Location loc = victimEntity.getLocation();
        this.drawCircle(loc, 3, uuid);
    }

    private void drawCircle(final Location center, final int size, final UUID uuid) {
        center.setY(this.floor.getLoc1().getY());
        final List<Block> circle = this.floor.circle(center, size);

        final WoolColors color = this.woolColorMap.get(uuid);
        circle.forEach(block -> {
            if (block.getType() != Material.WOOL || woolCountMap.getOrDefault(uuid, new ArrayList<>()).contains(block.getLocation())) return;
            block.setType(Material.WOOL);
            block.setData(color.getLegacyColorId());

            List<Location> loc = woolCountMap.getOrDefault(uuid, new ArrayList<>());
            loc.add(block.getLocation());

            woolCountMap.put(uuid, loc);
        });

        this.pop(center);
    }

    @EventHandler
    public void onMoveEvent(PlayerMoveEvent e) {
        // Display a action bar with the amount of wool the other players have
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();

        if (!super.containsUuid(uuid)) return;

        // Compile a string with each player wool count
        // Name - Wool Count | Name - Wool Count
        final StringBuilder sb = new StringBuilder();

        for (final Map.Entry<UUID, List<Location>> entry : this.woolCountMap.entrySet()) {
            final UUID otherUuid = entry.getKey();
            final int woolCount = entry.getValue().size();

            final Player otherPlayer = Bukkit.getPlayer(otherUuid);
            if (otherPlayer == null)
                continue;

            final String name = otherPlayer.getDisplayName();
            sb.append("&9").append(name).append(" &7- &9").append(woolCount);

            List<UUID> otherPlayerIndex = Arrays.asList(this.woolCountMap.keySet().toArray(new UUID[this.woolCountMap.size()]));

            if (otherPlayerIndex.get(otherPlayerIndex.size() - 1) != otherUuid)
                sb.append(" &7| ");
        }

        sendActionBar(p, sb.toString());
    }

    public static void sendActionBar(Player player, String message) {
        message= message.replaceAll("%player%", player.getDisplayName());
        message = ChatColor.translateAlternateColorCodes('&', message);
        CraftPlayer p = (CraftPlayer) player;
        IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        p.getHandle().playerConnection.sendPacket(ppoc);
    }

    @EventHandler
    public void onPlayerEggThrow(final PlayerEggThrowEvent e) {
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();
        final Egg egg = e.getEgg();
        e.setHatching(false);

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
        final Location loc = egg.getLocation();

        this.drawCircle(loc, 4, p.getUniqueId());
    }

    /**
     * To be called when a player is hit / egg lands
     * @param loc
     */
    private void pop(final Location loc) {
        loc.getWorld().playSound(loc, Sound.CHICKEN_EGG_POP, 1F, 1F);
    }

    @Override
    public boolean isEliminated(Player p) {
        for (final Map.Entry<UUID, List<Location>> entry : this.woolCountMap.entrySet()) {
            final int woolCount = entry.getValue().size();

            if (woolCount > this.woolCountMap.getOrDefault(p.getUniqueId(), new ArrayList<>()).size())
                return true;
        }

        return false;
    }
}