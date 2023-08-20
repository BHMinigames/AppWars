package me.bjedev.appwars.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@UtilityClass
public class MessagesUtils {
    public void sendMapEditError(Player p) {
        p.sendMessage(ChatColor.RED + "You are not allowed to modify the map there!");
    }

    public void sendNoPermissionError(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
    }

    public void sendMustBePlayer(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "You must be a player to perform this command.");
    }

    public void sendCannotDoThisAtThisTime(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "You cannot do this at this time!");
    }

    public void sendOutOfBoundsError(CommandSender sender) { sender.sendMessage(ChatColor.RED + "You can not go there!");
    }
}