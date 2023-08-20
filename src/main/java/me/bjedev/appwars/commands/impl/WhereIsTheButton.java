package me.bjedev.appwars.commands.impl;

import me.bjedev.appwars.game.GameManager;
import me.bjedev.appwars.game.arena.impl.Maps;
import me.bjedev.appwars.utils.MessagesUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Fluyd is a fucking gay loser kys
 */
public class WhereIsTheButton implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessagesUtils.sendMustBePlayer(sender);
            return true;
        }

        final Player p = (Player) sender;

        if (!p.hasPermission("rizon.admin")) {
            MessagesUtils.sendNoPermissionError(p);
            return false;
        }

        AddButton.Button button = ((Maps) GameManager.getArena("MAPS")).getCurrentButton();

        if (button == null) {
            p.sendMessage(ChatColor.RED + "Button does not exist.");
            return true;
        }

//        sender.sendMessage(ChatColor.GREEN + "The button is at: " + ChatColor.YELLOW + button.getPlaceOn());

        final Location loc = button.getPlaceOn();

        final int x = loc.getBlockX();
        final int y = loc.getBlockY();
        final int z = loc.getBlockZ();

        final TextComponent component = new TextComponent(ChatColor.GREEN + String.format("The button is at: " + ChatColor.YELLOW + "%s %s %s.", x, y, z));
        final String cmd = String.format("/tp %s %s %s", x, y, z);

        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.AQUA + cmd).create()));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));

        p.spigot().sendMessage(component);
        return true;
    }
}