package me.bjedev.appwars.listeners.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerKillEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player killer;
    private final Player victim;

    public PlayerKillEvent(Player killer, Player victim) {
        this.killer = killer;
        this.victim = victim;
    }

    public Player getKiller() {
        return killer;
    }

    public Player getVictim() {
        return victim;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}