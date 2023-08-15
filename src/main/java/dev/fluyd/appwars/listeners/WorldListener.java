package dev.fluyd.appwars.listeners;

import dev.fluyd.appwars.commands.impl.BuildMode;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.utils.GameState;
import dev.fluyd.appwars.utils.MessagesUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

public class WorldListener implements Listener {
    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        if (handleBuild(e, e.getBlock(), e.getPlayer(), null, false))
            return;

        e.setCancelled(true);
        MessagesUtils.sendMapEditError(e.getPlayer());
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent e) {
        if (handleBuild(e, e.getBlock(), e.getPlayer(), e.getBlockReplacedState(), true))
            return;

        e.setCancelled(true);
        MessagesUtils.sendMapEditError(e.getPlayer());
    }

    private boolean handleBuild(final Cancellable e, final Block block, final Player p, final BlockState replacedBlock, final boolean place) {
        final UUID uuid = p.getUniqueId();

        if (BuildMode.BUILD_MODE)
            return true;

        if (GameManager.state != GameState.STARTED)
            return false;

        if (!GameManager.players.containsKey(uuid))
            return false;

        final Arena arena = GameManager.players.get(uuid);
        if (arena == null)
            return false;

        if (!arena.isBuild())
            return false;

        // If the block is not a placed block and the block being replaced is null, then it's not a placed block
        if (!arena.isPlacedBlock(block) && !place)
            return false;

        e.setCancelled(false);
        arena.addPlacedBlock(block, replacedBlock);
        return true;
    }
}