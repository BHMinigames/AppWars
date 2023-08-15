package dev.fluyd.appwars.game;

import dev.fluyd.appwars.AppWars;
import dev.fluyd.appwars.utils.config.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

/**
 * Thanks Fluyd
 */
public final class Countdown {
    private int countdownTime;
    private final int originalCountdownTime;
    private BukkitTask task;

    public Countdown(final int countdownTime) {
        this.countdownTime = countdownTime;
        this.originalCountdownTime = countdownTime;
    }

    /**
     * Starts the countdown
     */
    public void start(final Runnable runnable) {
        task = Bukkit.getScheduler().runTaskTimer(AppWars.INSTANCE, () -> {
            if (Bukkit.getOnlinePlayers().size() < ConfigUtils.INSTANCE.minPlayers) {
                this.perform(p -> p.sendMessage(ChatColor.RED + "Countdown was cancelled, there are no longer enough players to start the game!"));
                task.cancel();
                return;
            }

            if (countdownTime == 0) {
                this.perform(p -> {
                    p.sendMessage(ChatColor.GREEN + "GO!");
                    playUniversalSound(p);
                });

                runnable.run();
                task.cancel();
            } else {
                this.perform(p -> {;
                    p.sendMessage(ChatColor.GREEN + "Starting in " + getColor(countdownTime) + countdownTime + ChatColor.GREEN + " second" + ChatColor.GREEN + (countdownTime == 1 ? "" : "s") + ChatColor.GREEN + "!");
                    playUniversalSound(p);
                });
            }

            --countdownTime;
        }, 0L, 20L);
    }

    /**
     * Performs action on all online players
     * @param callback
     */
    private void perform(final Consumer<Player> callback) {
        for (final Player op : Bukkit.getOnlinePlayers())
            callback.accept(op);
    }

    private ChatColor getColor(int remaining) {
        double ratio = remaining / (double) originalCountdownTime;

        if (ratio <= 1.0/3) { // bottom third
            return ChatColor.RED;
        } else if (ratio <= 2.0/3) { // middle third
            return ChatColor.YELLOW;
        } else { // top third
            return ChatColor.GREEN;
        }
    }

    private void playUniversalSound(Player player) {
        Sound soundToPlay = getCompatibleSound();

        if (soundToPlay != null) {
            player.playSound(player.getLocation(), soundToPlay, 1.0F, 1.0F);
        }
    }

    private Sound getCompatibleSound() {
        String version = Bukkit.getServer().getBukkitVersion().split("-")[0];

        // For example purposes, let's say:
        // - In 1.8, the sound was named "ORB_PICKUP"
        // - From 1.9 to 1.12, it was named "ENTITY_EXPERIENCE_ORB_PICKUP"
        // - From 1.13 onwards, let's assume it's named "ENTITY_EXPERIENCE_ORB_TOUCH"

        if (version.startsWith("1.8")) {
            return getSound("ORB_PICKUP");
        } else if (version.matches("1.(9|10|11|12).*")) {
            return getSound("ENTITY_EXPERIENCE_ORB_PICKUP");
        } else {
            return getSound("ENTITY_EXPERIENCE_ORB_TOUCH"); // For 1.13 and later
        }
    }

    private Sound getSound(String soundName) {
        try {
            return Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            return null;  // Sound doesn't exist in this version
        }
    }
}