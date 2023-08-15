package dev.fluyd.appwars.game.arena.impl;

import dev.fluyd.appwars.commands.impl.AddButton;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.game.arena.AboutArena;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.utils.GameState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Button;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/**
 * Effectively find the button
 */
@AboutArena(name = "MAPS", allowDropItems = true, subTitle = "&eFind the button before the time is up!")
public final class Maps extends Arena implements Listener {
    private AddButton.Button currentButton;

    @Override
    public void start() throws Exception {
        if (this.getButtons().isEmpty())
            throw new Exception("Maps has 0 button locations set");

        super.teleport();
        super.sendTitle();

        this.currentButton = this.getRandomButton();
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        if (GameManager.state != GameState.STARTED)
            return;

        if (this.currentButton == null)
            return;

        final Player p = e.getPlayer();
        if (!this.getPlayers().contains(p))
            return;

        final AddButton.Button button = AddButton.getButton(p);

        if (button == null)
            return;

        if (!button.equals(this.currentButton))
            return;

        this.placeButton(this.currentButton);
    }

    @EventHandler
    public void reset() {
        if (this.currentButton != null)
            this.currentButton.getPlaceOn().getBlock().getRelative(this.currentButton.getFace()).setType(Material.AIR);
    }

    private void placeButton(final AddButton.Button button) {
        final Block placeOn = button.getPlaceOn().getBlock();
        final Block buttonBlock = placeOn.getRelative(button.getFace());

        if (this.isButton(buttonBlock))
            return;

        Button b = new Button();
        b.setFacingDirection(button.getFace());

        buttonBlock.setType(b.getItemType());
        buttonBlock.setData(b.getData());
        buttonBlock.getState().update();
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if (GameManager.state != GameState.STARTED)
            return;

        if (this.currentButton == null)
            return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        final Block clicked = e.getClickedBlock();

        if (clicked == null)
            return;

        if (!this.isButton(clicked))
            return;

        final AddButton.Button button = new AddButton.Button(clicked.getRelative(e.getBlockFace().getOppositeFace()).getLocation(), this.currentButton.getFace());

        if (!this.currentButton.equals(button))
            return;

        final Player winner = e.getPlayer();
        if (!this.getPlayers().contains(winner))
            return;

        final Player loser = this.getOtherPlayer(winner);

        if (loser.getGameMode() == GameMode.SPECTATOR)
            return;

        GameManager.victory(winner, "&eYou won!");
        GameManager.eliminated(loser, "&eYou lost! Your opponent got to the button first.");

        loser.setGameMode(GameMode.SPECTATOR);
        loser.teleport(winner.getLocation());

        GameManager.eliminate(loser.getUniqueId());
    }

    private boolean isButton(final Block block) {
        return block.getType().name().endsWith("_BUTTON");
    }

    private AddButton.Button getRandomButton() {
        return this.getButtons().get(new Random().nextInt(this.getButtons().size()));
    }

    @Override
    public void enable(final JavaPlugin instance) {
        Bukkit.getPluginManager().registerEvents(this, instance);
    }
}