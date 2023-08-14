package dev.fluyd.appwars.commands;

import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.utils.MessagesUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Loc implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessagesUtils.sendMustBePlayer(sender);
            return true;
        }

        final Player p = (Player) sender;

        if (!p.hasPermission("rizon.loc")) {
            MessagesUtils.sendNoPermissionError(p);
            return true;
        }

        if (args.length < 2) {
            p.sendMessage(ChatColor.RED + command.getUsage());
            return true;
        }

        int i;
        try {
            i = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            p.sendMessage(ChatColor.RED + exception.getMessage());
            return true;
        }

        final String name = args[1];

        final Arena arena = GameManager.getArena(name);
        if (arena == null) {
            p.sendMessage(ChatColor.RED + "No such arena exists.");
            return true;
        }

        SetSpawn.center(p);

        final Location location = p.getLocation();
        if (i == 1)
            arena.setLoc1(location);
        else if (i == 2)
            arena.setLoc2(location);
        else
            arena.setViewLoc(location);

        arena.saveLocations();
        p.sendMessage(ChatColor.GREEN + "Saved location " + (i <= 2 ? i : "view") + " to arena " + arena.getName() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2) {
            return GameManager.arenas.stream()
                    .map(Arena::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}