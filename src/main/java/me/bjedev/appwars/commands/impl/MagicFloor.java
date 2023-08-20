package me.bjedev.appwars.commands.impl;

import me.bjedev.appwars.commands.ArenaTabComplete;
import me.bjedev.appwars.game.GameManager;
import me.bjedev.appwars.game.arena.Arena;
import me.bjedev.appwars.utils.MessagesUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class MagicFloor extends ArenaTabComplete implements CommandExecutor {
    private final Map<Arena, me.bjedev.appwars.game.arena.MagicFloor> magicFloors = new HashMap<>();

    public MagicFloor() {
        super(0);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            MessagesUtils.sendMustBePlayer(sender);
            return true;
        }

        final Player p = (Player) sender;

        if (!p.hasPermission("rizon.magicfloor")) {
            MessagesUtils.sendNoPermissionError(p);
            return true;
        }

        if (args.length < 1) {
            p.sendMessage(ChatColor.RED + command.getUsage());
            return true;
        }

        final String name = args[0];

        final Arena arena = GameManager.getArena(name);
        if (arena == null) {
            p.sendMessage(ChatColor.RED + "No such arena exists.");
            return true;
        }

        if (this.magicFloors.containsKey(arena)) {
            final me.bjedev.appwars.game.arena.MagicFloor floor = this.magicFloors.get(arena);
            floor.setLoc2(p.getLocation());

            this.magicFloors.remove(arena);
            arena.addMagicFloor(floor);
            arena.saveMagicFloors();

            p.sendMessage(ChatColor.GREEN + "Set the second location to a magic floor for " + arena.getName() + ".");
        } else {
            final me.bjedev.appwars.game.arena.MagicFloor floor = new me.bjedev.appwars.game.arena.MagicFloor();
            floor.setLoc1(p.getLocation());

            this.magicFloors.put(arena, floor);

            p.sendMessage(ChatColor.GREEN + "Set the first location to a magic floor for " + arena.getName() + ".");
        }

        return true;
    }
}