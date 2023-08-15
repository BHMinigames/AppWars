package dev.fluyd.appwars.commands.impl;

import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.utils.GameState;
import dev.fluyd.appwars.utils.MessagesUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class Start implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("rizon.start")) {
            MessagesUtils.sendNoPermissionError(sender);
            return false;
        }

        if (GameManager.state == GameState.STARTED) {
            sender.sendMessage(ChatColor.RED + "A game is already in progress...");
            return false;
        }

        final List<Player> validPlayers = new ArrayList<>();

        for (final Player p : Bukkit.getOnlinePlayers()) {
            if (GameManager.checkPlayerState(p))
                continue;

            validPlayers.add(p);
        }

        if (Bukkit.getOnlinePlayers().size() <= 1) {
            sender.sendMessage(ChatColor.RED + "You need at least 2 players to start the game!");
            return false;
        }

        if (validPlayers.size() <= 1) {
            sender.sendMessage(ChatColor.RED + "Some players have not respawned yet!");
            validPlayers.clear();
            return false;
        }

        GameManager.start();
        sender.sendMessage(ChatColor.GREEN + "Attempted to start game.");
        return true;
    }
}