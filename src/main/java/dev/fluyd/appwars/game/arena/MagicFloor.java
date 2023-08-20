package dev.fluyd.appwars.game.arena;

import dev.fluyd.appwars.AppWars;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.utils.GameState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public final class MagicFloor {
    public enum Type {
        LEGACY, NEW;
    }

    private Location loc1;
    private Location loc2;
    private BukkitTask task = null;
    private @Getter(AccessLevel.NONE) List<Location> locations;

    public MagicFloor(final Location loc1, final Location loc2) {
        this.loc1 = loc1;
        this.loc2 = loc2;
    }

    public void start() {
        this.start(Type.LEGACY);
    }

    public void start(final Type type) {
        if (task != null)
            return;

        this.task = Bukkit.getScheduler().runTaskTimer(AppWars.INSTANCE, () -> {
            if (GameManager.state != GameState.STARTED)
                this.cancel();

            final List<Block> blocks = this.getBlocks();

            switch (type) {
                case LEGACY: {
                    for (final Block block : blocks) {
                        final WoolColors color = WoolColors.getRandomColor();
                        this.sendBlockUpdate(block, Material.WOOL, color.getLegacyColorId());
                    }

                    break;
                }

                case NEW: {
                    final List<Block> placedCircleBlocks = new ArrayList<>();

                    for (int i = 0; i < 4; ++i) {
                        final Location rnd = blocks.get(new Random().nextInt(blocks.size())).getLocation();
                        final List<Block> circle = this.circle(rnd, 3);
                        final WoolColors color = WoolColors.getRandomColor();

                        placedCircleBlocks.addAll(circle);

                        for (final Block block : blocks)
                            if (circle.contains(block))
                                this.sendBlockUpdate(block, Material.WOOL, color.getLegacyColorId());
                    }

                    for (final Block block : blocks) {
                        if (placedCircleBlocks.contains(block))
                            continue;

                        this.sendBlockUpdate(block, block.getType() == Material.AIR ? Material.WOOL : block.getType(), block.getData());
                    }

                    break;
                }
            }
        }, 5L, 10L);
    }

    @Deprecated
    private void sendBlockUpdate(final Block block, final Material material, final byte data) {
        for (final Player p : Bukkit.getOnlinePlayers())
            p.sendBlockChange(block.getLocation(), material, data);
    }

    public void cancel() {
        this.task.cancel();
        this.task = null;
    }

    public List<Block> getBlocks() {
        if (this.locations != null)
            return this.locations.stream().map(location -> location.getBlock()).collect(Collectors.toList());

        List<Block> blocks = new ArrayList<>();

        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(loc1.getWorld().getBlockAt(x, y, z));
                }
            }
        }

        this.locations = blocks.stream().map(block -> block.getLocation()).collect(Collectors.toList());
        return blocks;
    }

    /**
     * Attempts to get a circle of blocks around a center location
     * @param center
     * @param radius
     * @return
     */
    public List<Block> circle(final Location center, final int radius) {
        final List<Block> blocks = new ArrayList<>();

        final int cx = center.getBlockX();
        final int cy = center.getBlockY();
        final int cz = center.getBlockZ();

        final World world  = center.getWorld();
        final int rSquared = radius * radius;

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                if ((cx - x) * (cx - x) + (cz - z) * (cz - z) <= rSquared)
                    blocks.add(world.getBlockAt(x, cy, z));
            }
        }

        return blocks;
    }
}