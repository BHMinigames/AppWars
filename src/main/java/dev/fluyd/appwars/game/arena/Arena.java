package dev.fluyd.appwars.game.arena;

import dev.fluyd.appwars.AppWars;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.utils.config.ConfigUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public abstract class Arena {
    private final double PHASE_SPEED_INCREMENT = 0.4;
    private final int PHASE_DURATION = 20;

    private final String name;
    private final boolean pvp;
    private final boolean build;
    private final boolean interact;
    private final boolean dropItems;
    private final boolean noFall;
    private final String subTitle;

    private final @Getter(AccessLevel.PROTECTED) List<Location> placedBlocks = new ArrayList<>();

    private Location loc1;
    private Location loc2;
    private Location viewLoc;

    private UUID uuid1;
    private UUID uuid2;

    public Arena() {
        final Annotation annotation = this.getClass().getAnnotation(AboutArena.class);
        final AboutArena about = (AboutArena) annotation;

        this.name = about.name();
        this.pvp = about.pvp();
        this.build = about.build();
        this.interact = about.allowInteraction();
        this.dropItems = about.allowDropItems();
        this.noFall = about.noFall();
        this.subTitle = about.subTitle();

        this.setLocations();
    }

    /**
     * Called when a new round is started
     */
    public abstract void start();

    private void setLocations() {
        this.loc1 = (Location) ConfigUtils.INSTANCE.config.get(String.format("%s.loc-1", this.name));
        this.loc2 = (Location) ConfigUtils.INSTANCE.config.get(String.format("%s.loc-2", this.name));
        this.viewLoc = (Location) ConfigUtils.INSTANCE.config.get(String.format("%s.view-loc", this.name));
    }

    public void saveLocations() {
        ConfigUtils.INSTANCE.config.set(String.format("%s.loc-1", this.name), this.loc1);
        ConfigUtils.INSTANCE.config.set(String.format("%s.loc-2", this.name), this.loc2);
        ConfigUtils.INSTANCE.config.set(String.format("%s.view-loc", this.name), this.viewLoc);

        ConfigUtils.INSTANCE.save();

        this.setLocations();
    }

    public void addPlacedBlock(final Block block) {
        this.placedBlocks.add(block.getLocation());
    }

    public boolean isPlacedBlock(final Block block) {
        return this.placedBlocks.contains(block.getLocation());
    }

    public void removePlacedBlocks() {
        if (!this.build)
            return;

        this.placedBlocks.forEach(block -> block.getBlock().setType(Material.AIR));
        this.placedBlocks.clear();
    }

    public void addPlayer(final UUID uuid) {
        if (this.uuid1 == null)
            this.uuid1 = uuid;
        else
            this.uuid2 = uuid;
    }

    public void clearPlayers() {
        this.uuid1 = null;
        this.uuid2 = null;
    }

    public Player getPlayer1() {
        return Bukkit.getPlayer(this.uuid1);
    }

    public Player getPlayer2() {
        return Bukkit.getPlayer(this.uuid2);
    }

    public List<Player> getPlayers() {
        return Arrays.asList(this.getPlayer1(), this.getPlayer2());
    }

    /**
     * TODO: Change this method to do the slow teleport where it pushes you into the app
     */
    protected void teleport() {
        final Player p1 = this.getPlayer1();
        final Player p2 = this.getPlayer2();

        if (p1 != null)
            this.phaseIn(p1, this.getLoc1());

        if (p2 != null)
            this.phaseIn(p2, this.getLoc2());
    }

    protected void sendTitle() {
        this.getPlayers().forEach(p -> p.sendTitle(ChatColor.translateAlternateColorCodes('&', String.format("&a&l%s", this.getName())), ChatColor.translateAlternateColorCodes('&', this.getSubTitle())));
    }

    /**
     * Check if the game started longer than x amount of seconds ago
     * @param seconds
     * @return
     */
    private boolean startedLongerThan(final long seconds) {
        final long now = Instant.now().getEpochSecond();
        final long diff = now - GameManager.startedAt;

        return diff > seconds;
    }

    /**
     * Phases the player into the app and then teleports them to the apps corresponding location
     */
    private void phaseIn(final Player p, final Location arenaLocation) {
        if (this.viewLoc == null || this.startedLongerThan(10)) {
            p.teleport(arenaLocation);
            return;
        }

        final float originalFlySpeed = p.getFlySpeed();

        p.teleport(this.getViewLoc());
        p.setGameMode(GameMode.SPECTATOR);
        p.setFlySpeed(0F);

        new BukkitRunnable() {
            double speed = 0.2;
            int count = 0;
            float pitch = 1F;

            @Override
            public void run() {
                if (count >= 3) {
                    super.cancel();

                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2 * 20, 0, false, false));
                    p.setFlySpeed(originalFlySpeed);
                    p.teleport(arenaLocation);
                    p.setGameMode(GameMode.SURVIVAL);
                    p.playSound(p.getLocation(), Sound.EXPLODE, 1f, 1f);
                    return;
                }

                for (int i = 0; i < count + 1; i++)
                    p.playSound(p.getLocation(), Sound.NOTE_PLING, 2f, pitch);

                pitch += 0.1F;

                final Location loc = p.getLocation();
                loc.setYaw(viewLoc.getYaw());
                loc.setPitch(viewLoc.getPitch());
                p.teleport(loc);

                Vector velocity = p.getLocation().getDirection().multiply(speed);
                p.setVelocity(velocity);

                speed += PHASE_SPEED_INCREMENT;
                count++;
            }
        }.runTaskTimer(AppWars.INSTANCE, 0, PHASE_DURATION);
    }
}