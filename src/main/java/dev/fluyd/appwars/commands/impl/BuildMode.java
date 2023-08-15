package dev.fluyd.appwars.commands.impl;

import dev.fluyd.appwars.utils.MessagesUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildMode implements CommandExecutor {
    public static boolean BUILD_MODE = false;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rizon.admin")) {
            MessagesUtils.sendNoPermissionError((Player) sender);
            return false;
        }

        BUILD_MODE = !BUILD_MODE;

        sender.sendMessage(ChatColor.GRAY + "Build mode toggled " + (BUILD_MODE ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF") + ChatColor.GRAY + ".");

        return false;
    }
}
