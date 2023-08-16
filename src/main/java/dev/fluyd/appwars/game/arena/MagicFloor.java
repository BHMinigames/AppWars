package dev.fluyd.appwars.game.arena;

import dev.fluyd.appwars.AppWars;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.utils.GameState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public final class MagicFloor {
    private Location loc1;
    private Location loc2;
    private BukkitTask task = null;

    public MagicFloor(final Location loc1, final Location loc2) {
        this.loc1 = loc1;
        this.loc2 = loc2;
    }

    public void start() {
        if (task != null)
            return;

        this.task = Bukkit.getScheduler().runTaskTimer(AppWars.INSTANCE, () -> {
            if (GameManager.state != GameState.STARTED)
                this.cancel();

            final List<Block> blocks = this.getBlocks();

            for (final Block block : blocks) {
                final WoolColors color = WoolColors.getRandomColor();
                this.sendBlockUpdate(block, Material.WOOL, color.getLegacyColorId());
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

    private List<Block> getBlocks() {
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

        return blocks;
    }
}