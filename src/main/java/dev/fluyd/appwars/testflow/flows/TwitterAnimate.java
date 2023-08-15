package dev.fluyd.appwars.testflow.flows;

import dev.fluyd.appwars.AppWars;
import dev.fluyd.appwars.testflow.TestFlow;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class TwitterAnimate implements TestFlow {

    private final double SPEED_INCREMENT = 0.4;  // Small increment for slower, smoother movement
    private final int DURATION = 20;  // In ticks. (20 ticks = 1 second)

    @Override
    public String getName() {
        return "TWITTER_ANIMATE";
    }

    @Override
    public void run(Player sender, String[] args) {
        // Teleport the player
        sender.teleport(new Location(sender.getWorld(), 8, 142, 15, 180, 0));

        // Change game mode to SPECTATOR
        sender.setGameMode(GameMode.SPECTATOR);

        // Start the movement task
        new BukkitRunnable() {
            double speed = 0.1;  // Starting speed
            int count = 0;
            float pitch = 1f;   // Starting pitch

            @Override
            public void run() {
                if (count >= 3) {
                    cancel();
                    // Apply blindness
                    sender.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2 * 20, 0, false, false));
                    // Teleport to arena
                    sender.teleport(new Location(sender.getWorld(), 3881, 148, 3991, 180, 0).add(.5, .5, .5));
                    // Change game mode to ADVENTURE
                    sender.setGameMode(GameMode.ADVENTURE);
                    // Play explosion sound
                    sender.playSound(sender.getLocation(), Sound.EXPLODE, 1f, 1f);
                    return;
                }

                for (int i = 0; i < count + 1; i++) {
                    // Play high-pitched ding sound
                    sender.playSound(sender.getLocation(), Sound.NOTE_PLING, 2f, pitch);
                }
                pitch += 0.1f; // Increase the pitch a bit for each ding

                Vector velocity = sender.getLocation().getDirection().multiply(speed);
                sender.setVelocity(velocity);

                speed += SPEED_INCREMENT;
                count++;
            }
        }.runTaskTimer(AppWars.INSTANCE, 0, DURATION);
    }
}
