package dev.fluyd.appwars.game;

import dev.fluyd.appwars.AppWars;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.game.arena.impl.Mail;
import dev.fluyd.appwars.game.arena.impl.Maps;
import dev.fluyd.appwars.game.arena.impl.Parkour;
import dev.fluyd.appwars.game.arena.impl.Twitter;
import dev.fluyd.appwars.mirror.Mirror;
import dev.fluyd.appwars.utils.GameState;
import dev.fluyd.appwars.utils.config.ConfigUtils;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
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
    public final Map<Player, Mirror> playerMirrors = new HashMap<>();

    public long startedAt = 0;
    public long roundStartedAt = 0;

    public void initArenas() {
        newArena(new Twitter());
        newArena(new Maps());
        newArena(new Parkour());
        newArena(new Mail());
    }

    private void newArena(final Arena arena) {
        if (!arenas.contains(arena))
            arenas.add(arena);

        arena.enable(AppWars.INSTANCE);
    }

    public void disable() {
        GameManager.resetGame();
        Bukkit.getOnlinePlayers().forEach(p -> p.kickPlayer(ChatColor.RED + "Game restarting")); // To prevent bugs on reload

        playerMirrors.values().forEach(Mirror::deleteNPC);
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
                if (state == GameState.WAITING) {
                    super.cancel();
                    return;
                }

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
                    assignArenas(new HashSet<>(GameManager.players.keySet())); // Also starts the arena
                } catch (final Exception exception) {
                    exception.printStackTrace();
                    super.cancel();
                }
            }
        }.runTaskTimer(AppWars.INSTANCE, 5L, roundLengthTicks);
    }

    public void sendTitle(final Player p, final String title, final String subTitle) {
        if (p == null)
            return;

        p.sendTitle(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subTitle));
    }

    public void victory(final Player p, final String subTitle) {
        sendTitle(p, "&a&lVictory", subTitle);
    }

    public void eliminated(final Player p, final String subTitle) {
        sendTitle(p, "&c&lEliminated", subTitle);
    }

    public void resetGame() {
        GameManager.players.clear();
        GameManager.state = GameState.WAITING;

        if (ConfigUtils.INSTANCE.lobbyLocation != null)
            Bukkit.getOnlinePlayers().forEach(GameManager::reset);

        GameManager.arenas.forEach(GameManager::resetArena);
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

            if (checkPlayerState(p))
                toEliminate.add(uuid);

            if (arena.isEliminated(p))
                toEliminate.add(uuid);
        });

        toEliminate.forEach(uuid -> {
            eliminate(uuid);
        });
    }

    /**
     * Check whether specified player is eliminated or not
     * @param p
     * @return
     */
    public boolean checkPlayerState(final Player p) {
        if (p == null || !p.isOnline())
            return true;

        if (p.isDead() || p.getGameMode() == GameMode.SPECTATOR)
            return true;

        return false;
    }

    public void eliminate(final UUID uuid) {
        GameManager.players.remove(uuid);

        final Player p = Bukkit.getPlayer(uuid);
        if (p != null)
            Bukkit.getOnlinePlayers().forEach(op -> op.sendMessage(ChatColor.RED + String.format("%s was eliminated!", p.getName())));
    }

    private void assignArenas(final Set<UUID> uuids) throws Exception {
        GameManager.players.clear();
        GameManager.arenas.forEach(GameManager::resetArena);

        final List<Arena> randomizedArenas = arenas.stream().filter(Arena::isAvailable).collect(Collectors.toList());
        Collections.shuffle(randomizedArenas);

        final Iterator<Arena> iterator = randomizedArenas.iterator();

        int playersAssigned = 0;
        List<? extends Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (!uuids.isEmpty())
            players = new ArrayList<>(uuids.stream().map(Bukkit::getPlayer).filter(p -> !checkPlayerState(p)).collect(Collectors.toList()));

        Collections.shuffle(players);

        Arena arena = null;
        final boolean notEven = players.size() % 2 != 0;

        for (final Player op : players) {
            ++playersAssigned;

            if (playersAssigned % 2 != 0) {
                if (!iterator.hasNext())
                    throw new Exception("Not enough arenas to start game.");

                arena = iterator.next();
            }

            if (notEven && playersAssigned == players.size())
                arena = GameManager.getArena("PARKOUR");

            final UUID uuid = op.getUniqueId();

            if (!GameManager.players.containsKey(uuid)) {
                GameManager.players.put(uuid, arena);
                arena.addPlayer(uuid);
            }

            if (!GameManager.playerMirrors.containsKey(op))
                GameManager.playerMirrors.put(op, new Mirror(op));
        }

        for (final Arena a : GameManager.arenas) {
            if (!a.getPlayers().isEmpty())
                a.start();
        }
    }

    private void resetArena(final Arena arena) {
        arena.reset();
        arena.removePlacedBlocks();

        arena.getPlayers().forEach(p -> {
            GameManager.clearInv(p);
            p.setHealth(p.getMaxHealth());
        });

        arena.clearPlayers();

        removeDroppedItems(arena.getLoc1().getWorld());
    }

    private void removeDroppedItems(final World world) {
        world.getEntities().stream().filter(entity -> entity.getType() == EntityType.DROPPED_ITEM).forEach(Entity::remove);
    }

    /**
     * Teleport player to spawn and set their GameMode to survival
     * @param p
     */
    public void reset(final Player p) {
        clearInv(p);

        p.setGameMode(GameMode.SURVIVAL);
        p.setHealth(p.getMaxHealth());
        p.getActivePotionEffects().clear();

        if (ConfigUtils.INSTANCE.lobbyLocation != null)
            p.teleport(ConfigUtils.INSTANCE.lobbyLocation.clone().add(0, 1, 0));

        if (p.getFlySpeed() <= 0F)
            p.setFlySpeed(2F);
    }

    /**
     * Clears the players inventory
     * @param p
     */
    public void clearInv(final Player p) {
        final PlayerInventory inv = p.getInventory();

        inv.clear();

        inv.setHelmet(null);
        inv.setChestplate(null);
        inv.setLeggings(null);
        inv.setBoots(null);
        p.setItemOnCursor(null);
    }
}