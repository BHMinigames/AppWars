package me.bjedev.appwars.game.arena.impl;

import me.bjedev.appwars.game.arena.AboutArena;
import me.bjedev.appwars.game.arena.Arena;
import me.bjedev.appwars.utils.ItemManager;
import org.bukkit.entity.Player;

@AboutArena(name = "TWITTER", pvp = true, build = true, allowInteraction = true, allowDropItems = true, subTitle = "&eWin the duel against your opponent!")
public final class Twitter extends Arena {
    @Override
    public void start() {
        super.teleport();
        super.sendTitle();

        for (final Player p : super.getPlayers())
            ItemManager.giveTwitterKit(p);
    }
}