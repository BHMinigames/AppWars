package dev.fluyd.appwars.commands;

import dev.fluyd.appwars.utils.MessagesUtils;
import dev.fluyd.appwars.utils.config.ConfigUtils;
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

        p.teleport(ConfigUtils.INSTANCE.lobbyLocation);
        p.sendMessage(ChatColor.GREEN + "Teleported to spawn.");
        return true;
    }
}