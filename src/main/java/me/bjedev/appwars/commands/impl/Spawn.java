package me.bjedev.appwars.commands.impl;

import me.bjedev.appwars.game.GameManager;
import me.bjedev.appwars.utils.GameState;
import me.bjedev.appwars.utils.MessagesUtils;
import me.bjedev.appwars.utils.config.ConfigUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Spawn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessagesUtils.sendMustBePlayer(sender);
            return false;
        }

        final Player p = (Player) sender;

        if (ConfigUtils.INSTANCE.lobbyLocation == null) {
            p.sendMessage(ChatColor.RED + "No spawn set.");
            return true;
        }

        if (GameManager.state == GameState.STARTED) {
            MessagesUtils.sendCannotDoThisAtThisTime(p);
            return true;
        }

        p.teleport(ConfigUtils.INSTANCE.lobbyLocation.clone().add(0, 1, 0));
        p.sendMessage(ChatColor.GREEN + "Teleported to spawn.");
        return true;
    }
}