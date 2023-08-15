package dev.fluyd.appwars.game.arena.impl;

import dev.fluyd.appwars.game.arena.AboutArena;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.game.arena.MagicFloor;

@AboutArena(name = "PARKOUR", subTitle = "&eWe did not find you an opponent, here is some parkour.", available = false)
public final class Parkour extends Arena {
    @Override
    public void start() {
        super.teleport();
        super.sendTitle();

        if (!super.startedLongerThan(10))
            this.getMagicFloors().values().forEach(MagicFloor::start);
    }
}