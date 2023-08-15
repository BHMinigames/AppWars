package dev.fluyd.appwars.commands.impl;

import dev.fluyd.appwars.commands.ArenaTabComplete;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.utils.MessagesUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;

public final class AddButton extends ArenaTabComplete implements CommandExecutor {
    public AddButton() {
        super(0);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            MessagesUtils.sendMustBePlayer(sender);
            return true;
        }

        final Player p = (Player) sender;

        if (!p.hasPermission("rizon.addbutton")) {
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

        final Button button = getButton(p);
        if (button == null || button.getFace() == null || button.getPlaceOn() == null) {
            p.sendMessage(ChatColor.RED + "The block the button should be placed could not be determined.");
            return true;
        }

        if (arena.hasButton(button)) {
            p.sendMessage(ChatColor.RED + "This arena already has this button.");
            return true;
        }

        arena.addButton(button);
        arena.saveButtons();

        p.sendMessage(ChatColor.GREEN + "Successfully added button to arena " + arena.getName() + ".");
        return true;
    }

    public static Button getButton(final Player player) {
        final List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks((HashSet<Byte>) null, 100);
        if (lastTwoTargetBlocks.size() != 2 || !lastTwoTargetBlocks.get(1).getType().isOccluding())
            return null;

        final Block targetBlock = lastTwoTargetBlocks.get(1);
        final Block adjacentBlock = lastTwoTargetBlocks.get(0);

        return new Button(targetBlock.getLocation(), targetBlock.getFace(adjacentBlock));
    }

    @AllArgsConstructor
    @Getter
    public static final class Button {
        private final Location placeOn;
        private final BlockFace face;

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof Button))
                return false;

            final Button button = (Button) obj;
            if (!button.getPlaceOn().equals(this.getPlaceOn()))
                return false;

            if (button.getFace() != this.getFace())
                return false;

            return true;
        }
    }
}