package dev.fluyd.appwars.listeners;

import dev.fluyd.appwars.commands.BuildModeCommand;
import dev.fluyd.appwars.utils.MessagesUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class WorldListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
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
}
