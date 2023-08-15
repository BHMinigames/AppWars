package dev.fluyd.appwars.commands;

import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.game.arena.Arena;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ArenaTabComplete implements TabCompleter {
    private final int index;

    public ArenaTabComplete(final int index) {
        this.index = index;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == index + 1) {
            return GameManager.arenas.stream()
                    .map(Arena::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[index].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}