package me.bjedev.appwars.game.arena.impl;

import lombok.Getter;
import me.bjedev.appwars.commands.impl.AddButton;
import me.bjedev.appwars.game.GameManager;
import me.bjedev.appwars.game.arena.AboutArena;
import me.bjedev.appwars.game.arena.Arena;
import me.bjedev.appwars.utils.GameState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Button;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.UUID;

/**
 * Effectively find the button
 */
@AboutArena(name = "MAPS", allowDropItems = true, allowInteraction = true, subTitle = "&eFind the button before the time is up!")
public final class Maps extends Arena implements Listener {
    private @Getter AddButton.Button currentButton;
    private UUID winner = null;

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
        if (!this.containsUuid(p.getUniqueId()))
            return;

        final AddButton.Button button = AddButton.getButton(p);

        if (button == null)
            return;

        if (!button.equals(this.currentButton))
            return;

        final double distance = button.getPlaceOn().distance(p.getLocation());
        if (distance > 10D) // If they are more than 10 blocks way do nothing
            return;

        this.placeButton(this.currentButton);
    }

    @Override
    public void reset() {
        if (this.currentButton != null)
            this.currentButton.getPlaceOn().getBlock().getRelative(this.currentButton.getFace()).setType(Material.AIR);

        this.currentButton = null;
        this.winner = null;
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

        if (this.winner != null)
            return;

        final Block clicked = e.getClickedBlock();

        if (clicked == null)
            return;

        if (!this.isButton(clicked))
            return;

        final AddButton.Button button = new AddButton.Button(clicked.getRelative(this.currentButton.getFace().getOppositeFace()).getLocation(), this.currentButton.getFace());

        if (!this.currentButton.equals(button))
            return;

        final Player winner = e.getPlayer();
        if (!this.containsUuid(winner.getUniqueId()))
            return;

        if (GameManager.checkPlayerState(winner))
            return;

        final Player loser = this.getOtherPlayer(winner);

        if (GameManager.checkPlayerState(loser) && loser != null && loser.isOnline())
            return;

        GameManager.victory(winner, "&eYou won the round against your opponent!");

        this.winner = winner.getUniqueId();

        if (loser != null) {
            GameManager.eliminated(loser, "&eYou lost! Your opponent got to the button first.");
            loser.setGameMode(GameMode.SPECTATOR);
            loser.teleport(winner.getLocation());
            GameManager.eliminate(loser.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
        if (this.winner == null)
            return;

        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();

        if (!this.containsUuid(uuid))
            return;

        if (this.winner.equals(uuid))
            return;

        p.setGameMode(GameMode.SPECTATOR);
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