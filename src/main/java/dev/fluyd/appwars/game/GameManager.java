package dev.fluyd.appwars.game;

import dev.fluyd.appwars.AppWars;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.game.arena.impl.Twitter;
import dev.fluyd.appwars.utils.GameState;
import dev.fluyd.appwars.utils.config.ConfigUtils;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public final class GameManager {
    public static long roundLength;
    public static long roundLengthTicks;

    public GameState state = GameState.WAITING;
    public final List<Arena> arenas = new ArrayList<>();
    public final Map<UUID, Arena> players = new HashMap<>();

    public long startedAt = 0;
    public long roundStartedAt = 0;

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
        GameManager.state = GameState.STARTED;
        GameManager.startedAt = Instant.now().getEpochSecond();

        new BukkitRunnable() {
            @Override
            public void run() {
                checkEveryPlayersSate();

                if (GameManager.players.size() == 1) { // A player won the game
                    GameManager.players.keySet().forEach(uuid -> {
                        final Player p = Bukkit.getPlayer(uuid);
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', String.format("&a%s won the game!", p.getName())));
                    });

                    resetGame();
                    super.cancel();

                    return;
                }

                if (GameManager.startedLongerThan(10) && GameManager.players.isEmpty()) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&bGame was cancelled, everyone left."));
                    resetGame();
                    super.cancel();
                    return;
                }

                try {
                    GameManager.roundStartedAt = Instant.now().getEpochSecond();
                    assignArenas(GameManager.players.keySet()); // Also starts the arena
                } catch (final Exception exception) {
                    exception.printStackTrace();
                    super.cancel();
                }
            }
        }.runTaskTimer(AppWars.INSTANCE, 5L, roundLengthTicks);
    }

    private void resetGame() {
        GameManager.players.clear();
        GameManager.state = GameState.WAITING;

        if (ConfigUtils.INSTANCE.lobbyLocation != null)
            Bukkit.getOnlinePlayers().forEach(p -> reset(p));

        GameManager.arenas.forEach(arena -> resetArena(arena));
    }

    /**
     * Check if the game started longer than x amount of seconds ago
     * @param seconds
     * @return
     */
    public boolean startedLongerThan(final long seconds) {
        final long now = Instant.now().getEpochSecond();
        final long diff = now - GameManager.startedAt;

        return diff > seconds;
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

        toEliminate.forEach(uuid -> {
            GameManager.players.remove(uuid);

            final Player p = Bukkit.getPlayer(uuid);
            if (p != null)
                Bukkit.getOnlinePlayers().forEach(op -> op.sendMessage(ChatColor.RED + String.format("%s was eliminated!", p.getName())));
        });
    }

    private void assignArenas(final Set<UUID> uuids) throws Exception {
        GameManager.players.clear();
        final Iterator<Arena> iterator = arenas.iterator();

        int playersAssigned = 0;
        List<? extends Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (!uuids.isEmpty())
            players = new ArrayList<>(uuids.stream().map(uuid -> Bukkit.getPlayer(uuid)).filter(p -> p != null && p.isOnline()).collect(Collectors.toList()));

        Collections.shuffle(players);

        Arena arena = null;
        for (final Player op : players) {
            ++playersAssigned;

            if (playersAssigned % 2 != 0) {
                if (!iterator.hasNext())
                    throw new Exception("Not enough arenas to start game.");

                arena = iterator.next();
                resetArena(arena);
            }

            final UUID uuid = op.getUniqueId();

            if (!GameManager.players.containsKey(uuid)) {
                GameManager.players.put(uuid, arena);
                arena.addPlayer(uuid);
            }
        }

        for (final Arena a : GameManager.arenas)
            a.start();
    }

    private void resetArena(final Arena arena) {
        arena.removePlacedBlocks();
        arena.clearPlayers();

        removeDroppedItems(arena.getLoc1().getWorld());
    }

    private void removeDroppedItems(final World world) {
        world.getEntities().stream().filter(entity -> entity.getType() == EntityType.DROPPED_ITEM).forEach(entity -> entity.remove());
    }

    /**
     * Teleport player to spawn and set their GameMode to survival
     * @param p
     */
    public void reset(final Player p) {
        if (ConfigUtils.INSTANCE.lobbyLocation != null)
            p.teleport(ConfigUtils.INSTANCE.lobbyLocation);

        p.getInventory().clear();
        p.setGameMode(GameMode.SURVIVAL);
        p.setHealth(p.getMaxHealth());

        if (p.getFlySpeed() <= 0F)
            p.setFlySpeed(2F);
    }
}