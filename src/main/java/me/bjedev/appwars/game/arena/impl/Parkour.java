package me.bjedev.appwars.game.arena.impl;

import me.bjedev.appwars.game.arena.AboutArena;
import me.bjedev.appwars.game.arena.Arena;
import me.bjedev.appwars.game.arena.MagicFloor;

@AboutArena(name = "PARKOUR", subTitle = "&eWe did not find you an opponent, here is some parkour.", available = false)
public final class Parkour extends Arena {
    @Override
    public void start() {
        super.teleport();
        super.sendTitle();
        super.startMagicFloors(MagicFloor.Type.LEGACY);
    }
}