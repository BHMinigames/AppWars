package dev.fluyd.appwars.game.arena.impl;

import dev.fluyd.appwars.game.arena.AboutArena;
import dev.fluyd.appwars.game.arena.Arena;

@AboutArena(name = "TWITTER", pvp = true, build = true, allowInteraction = true, allowDropItems = true)
public final class Twitter extends Arena {
    @Override
    public void start() {
        super.teleport();

    }

    
}