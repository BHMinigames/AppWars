package dev.fluyd.appwars.listeners;

import dev.fluyd.appwars.commands.BuildModeCommand;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.utils.GameState;
import dev.fluyd.appwars.utils.MessagesUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.UUID;

public class WorldListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (handleBuild(e, e.getBlock(), e.getPlayer()))
            return;

        if (BuildModeCommand.BUILD_MODE) return;

        e.setCancelled(true);
        MessagesUtils.sendMapEditError(e.getPlayer());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (BuildModeCommand.BUILD_MODE) return;

        e.setCancelled(true);
        MessagesUtils.sendMapEditError(e.getPlayer());
    }

    private boolean handleBuild(final Cancellable e, final Block block, final Player p) {
        final UUID uuid = p.getUniqueId();

        if (GameManager.state != GameState.STARTED)
            return false;

        if (!GameManager.players.containsKey(uuid))
            return false;

        final Arena arena = GameManager.players.get(uuid);
        if (arena == null)
            return false;

        if (!arena.isBuild())
            return false;

        e.setCancelled(false);
        arena.addPlacedBlock(block);
        return true;
    }
}