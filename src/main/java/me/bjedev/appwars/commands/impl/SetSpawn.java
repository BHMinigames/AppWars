package me.bjedev.appwars.commands.impl;

import me.bjedev.appwars.utils.MessagesUtils;
import me.bjedev.appwars.utils.config.ConfigType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SetSpawn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessagesUtils.sendMustBePlayer(sender);
            return false;
        }

        final Player p = (Player) sender;

        if (!p.hasPermission("rizon.setspawn")) {
            MessagesUtils.sendNoPermissionError(p);
            return false;
        }

        center(p);
        fixRotation(p);

        final Location loc = p.getLocation();
        ConfigType.LOBBY_LOCATION.set(loc);

        p.sendMessage(ChatColor.GREEN + "Spawn location has been set!");
        return true;
    }

    public static void center(final Player p) {
        final Location loc = p.getLocation();
        final Location center = new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY() + 0.5, loc.getBlockZ() + 0.5, loc.getYaw(), loc.getPitch());

        p.teleport(center);
    }

    private static void fixRotation(final Player p) {
        final Location loc = p.getLocation();
        loc.setYaw(180);
        loc.setPitch(0);

        p.teleport(loc);
    }
}