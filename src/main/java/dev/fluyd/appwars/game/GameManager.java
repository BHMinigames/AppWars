package dev.fluyd.appwars.game;

import dev.fluyd.appwars.AppWars;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.game.arena.impl.Twitter;
import dev.fluyd.appwars.utils.GameState;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@UtilityClass
public final class GameManager {
    public static final long ROUND_LENGTH = 60L * 20L; // 1 minute rounds
    /**
     * TODO: Just check everything (such as players being online still, still alive, etc at the end of every round and then determine the winner for that arena
     */

    public GameState state = GameState.WAITING;
    public final List<Arena> arenas = new ArrayList<>();
    public final Map<UUID, Arena> players = new HashMap<>();

    public void initArenas() {
        newArena(new Twitter());
    }

    private void newArena(final Arena arena) {
        if (!arenas.contains(arena))
            arenas.add(arena);
    }

    public void disable() {
        arenas.forEach(Arena::removePlacedBlocks);
    }

    /**
     * Gets an arena from name regardless of capitalization
     * @param name
     * @return
     */
    public Arena getArena(final String name) {
        for (final Arena arena : arenas)
            if (arena.getName().equalsIgnoreCase(name))
                return arena;

        return null;
    }

    /**
     * Start the game
     */
    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkEveryPlayersSate();

                if (GameManager.players.size() == 1) { // A player won the game
                    GameManager.players.keySet().forEach(uuid -> {
                        final Player p = Bukkit.getPlayer(uuid);
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', String.format("&a&l%s won the game!", p.getName())));
                    });

                    GameManager.players.clear();
                    GameManager.state = GameState.WAITING;

                    super.cancel();
                    return;
                }

                try {
                    assignArenas(); // Also starts the arena
                } catch (final Exception exception) {
                    exception.printStackTrace();
                    super.cancel();
                }
            }
        }.runTaskTimer(AppWars.INSTANCE, 5L, ROUND_LENGTH);
    }

    private void checkEveryPlayersSate() {
        final List<UUID> toEliminate = new ArrayList<>();

        GameManager.players.forEach((uuid, arena) -> {
            final Player p = Bukkit.getPlayer(uuid);

            if (p == null || !p.isOnline()) {
                toEliminate.add(uuid);
                return;
            }

            if (p.isDead() || p.getGameMode() == GameMode.SPECTATOR)
                toEliminate.add(uuid);
        });

        toEliminate.forEach(uuid -> GameManager.players.remove(uuid));
    }

    private void assignArenas() throws Exception {
        GameManager.players.clear();
        final Iterator<Arena> iterator = arenas.iterator();

        int playersAssigned = 0;
        final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        Collections.shuffle((List<?>) players);

        for (final Player op : players) {
            Arena arena = null;
            ++playersAssigned;

            if (playersAssigned % 2 == 0) {
                if (!iterator.hasNext())
                    throw new Exception("Not enough arenas to start game.");

                arena = iterator.next();
                arena.clearPlayers();
            }

            final UUID uuid = op.getUniqueId();
            GameManager.players.put(uuid, arena);

            arena.addPlayer(uuid);
        }

        for (final Arena arena : GameManager.players.values())
            arena.start();
    }
}