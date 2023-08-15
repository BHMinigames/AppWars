package dev.fluyd.appwars.commands.impl;

import dev.fluyd.appwars.commands.ArenaTabComplete;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.utils.MessagesUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class MagicFloor extends ArenaTabComplete implements CommandExecutor {
    private final Map<Arena, dev.fluyd.appwars.game.arena.MagicFloor> magicFloors = new HashMap<>();

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
            final dev.fluyd.appwars.game.arena.MagicFloor floor = this.magicFloors.get(arena);
            floor.setLoc2(p.getLocation());

            this.magicFloors.remove(arena);
            arena.addMagicFloor(floor);
            arena.saveMagicFloors();

            p.sendMessage(ChatColor.GREEN + "Set the second location to a magic floor for " + arena.getName() + ".");
        } else {
            final dev.fluyd.appwars.game.arena.MagicFloor floor = new dev.fluyd.appwars.game.arena.MagicFloor();
            floor.setLoc1(p.getLocation());

            this.magicFloors.put(arena, floor);

            p.sendMessage(ChatColor.GREEN + "Set the first location to a magic floor for " + arena.getName() + ".");
        }

        return true;
    }
}