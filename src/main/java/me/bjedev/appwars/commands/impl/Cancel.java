package me.bjedev.appwars.commands.impl;

import me.bjedev.appwars.game.GameManager;
import me.bjedev.appwars.utils.GameState;
import me.bjedev.appwars.utils.MessagesUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class Cancel implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("rizon.admin")) {
            MessagesUtils.sendNoPermissionError(sender);
            return true;
        }

        sender.sendMessage(GameManager.state == GameState.STARTED ?
                ChatColor.GREEN + "Successfully cancelled the game." :
                ChatColor.RED + "There is no game running.");

        if (GameManager.state == GameState.STARTED) {
            GameManager.resetGame();
            Bukkit.broadcastMessage(ChatColor.RED + "The game was cancelled by an admin.");
        }

        return true;
    }
}