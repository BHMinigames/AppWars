package dev.fluyd.appwars.game.arena;

import dev.fluyd.appwars.utils.config.ConfigUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public abstract class Arena {
    private final String name;
    private final boolean pvp;
    private final boolean build;
    private final boolean interact;
    private final boolean dropItems;

    private final @Getter(AccessLevel.PROTECTED) List<Location> placedBlocks = new ArrayList<>();

    private Location loc1;
    private Location loc2;

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

        this.setLocations();
    }

    public abstract void start();

    private void setLocations() {
        this.loc1 = (Location) ConfigUtils.INSTANCE.config.get(String.format("%s.loc-1", this.name));
        this.loc2 = (Location) ConfigUtils.INSTANCE.config.get(String.format("%s.loc-2", this.name));
    }

    public void saveLocations() {
        ConfigUtils.INSTANCE.config.set(String.format("%s.loc-1", this.name), loc1);
        ConfigUtils.INSTANCE.config.set(String.format("%s.loc-2", this.name), loc1);

        ConfigUtils.INSTANCE.save();

        this.setLocations();
    }

    public void addPlacedBlock(final Block block) {
        this.placedBlocks.add(block.getLocation());
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
        else if (this.uuid2 == null)
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
            p1.teleport(this.getLoc1());

        if (p2 != null)
            p2.teleport(this.getLoc2());
    }
}