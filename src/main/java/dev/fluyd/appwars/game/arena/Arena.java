package dev.fluyd.appwars.game.arena;

import dev.fluyd.appwars.AppWars;
import dev.fluyd.appwars.commands.impl.AddButton;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.utils.config.ConfigUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.annotation.Annotation;
import java.util.*;

@Getter
@Setter
public abstract class Arena {
    private final double PHASE_SPEED_INCREMENT = 0.4;
    private final int PHASE_DURATION = 20;

    private final String name;
    private final boolean pvp;
    private final boolean build;
    private final boolean interact;
    private final boolean dropItems;
    private final boolean noFall;
    private final String subTitle;
    private final boolean available;

    private final @Getter(AccessLevel.PROTECTED) HashMap<Location, BlockState> placedBlocks = new HashMap<>();

    private Location loc1;
    private Location loc2;
    private Location viewLoc;

    private UUID uuid1;
    private UUID uuid2;

    private @Getter(AccessLevel.PROTECTED) Map<Integer, AddButton.Button> buttons = new HashMap<>();
    private @Getter(AccessLevel.PROTECTED) Map<Integer, MagicFloor> magicFloors = new HashMap<>();

    public Arena() {
        final Annotation annotation = this.getClass().getAnnotation(AboutArena.class);
        final AboutArena about = (AboutArena) annotation;

        this.name = about.name();
        this.pvp = about.pvp();
        this.build = about.build();
        this.interact = about.allowInteraction();
        this.dropItems = about.allowDropItems();
        this.noFall = about.noFall();
        this.subTitle = about.subTitle();
        this.available = about.available();

        this.setLocations();
        this.setButtons();
        this.setMagicFloors();
    }

    /**
     * Called when plugin is enabled by Bukkit
     */
    public void enable(final JavaPlugin instance) {}

    /**
     * Called when the arena is reset
     */
    public void reset() {}

    /**
     * Called when a new round is started
     * Only called if there are players in this arena
     */
    public abstract void start() throws Exception;

    private void setLocations() {
        this.loc1 = (Location) ConfigUtils.INSTANCE.config.get(String.format("%s.loc-1", this.name));
        this.loc2 = (Location) ConfigUtils.INSTANCE.config.get(String.format("%s.loc-2", this.name));
        this.viewLoc = (Location) ConfigUtils.INSTANCE.config.get(String.format("%s.view-loc", this.name));
    }

    public void saveLocations() {
        ConfigUtils.INSTANCE.config.set(String.format("%s.loc-1", this.name), this.loc1);
        ConfigUtils.INSTANCE.config.set(String.format("%s.loc-2", this.name), this.loc2);
        ConfigUtils.INSTANCE.config.set(String.format("%s.view-loc", this.name), this.viewLoc);

        ConfigUtils.INSTANCE.save();

        this.setLocations();
    }

    public void addPlacedBlock(final Block block, final BlockState replacedBlock) {
        this.placedBlocks.put(block.getLocation(), replacedBlock);
    }

    public boolean isPlacedBlock(final Block block) {
        return this.placedBlocks.containsKey(block.getLocation());
    }

    public void removePlacedBlocks() {
        if (!this.build)
            return;

        this.placedBlocks.forEach((block, replacedBlock) -> {
            if (replacedBlock == null) return;
            block.getBlock().setType(replacedBlock.getType());
            block.getBlock().setData(replacedBlock.getRawData());
        });
        this.placedBlocks.clear();
    }

    public void addPlayer(final UUID uuid) {
        if (this.uuid1 == null)
            this.uuid1 = uuid;
        else
            this.uuid2 = uuid;
    }

    public void clearPlayers() {
        this.uuid1 = null;
        this.uuid2 = null;
    }

    private void setButtons() {
        final ConfigurationSection section = ConfigUtils.INSTANCE.config.getConfigurationSection(String.format("%s.buttons", this.name));

        if (section == null)
            return;

        section.getKeys(false).forEach(id -> {
            final Location placeOn = (Location) section.get(String.format("%s.place-on", id));
            final String blockFace = section.getString(String.format("%s.block-face", id));

            final AddButton.Button button = new AddButton.Button(placeOn, BlockFace.valueOf(blockFace));
            this.buttons.put(Integer.valueOf(id), button);
        });
    }

    /**
     * Add a button location to this arena
     * @param button
     */
    public void addButton(final AddButton.Button button) {
        final int nextId = this.buttons.size();
        this.buttons.put(nextId, button);
    }

    /**
     * Checks if this arena already has specified button
     * @param button
     * @return
     */
    public boolean hasButton(final AddButton.Button button) {
        for (final AddButton.Button b : this.buttons.values())
            if (b.equals(button))
                return true;

        return false;
    }

    /**
     * Save all the buttons in the arena to the config
     */
    public void saveButtons() {
        final String sectionName = String.format("%s.buttons", this.name);
        ConfigurationSection section = ConfigUtils.INSTANCE.config.getConfigurationSection(sectionName);

        if (ConfigUtils.INSTANCE.config.isConfigurationSection(sectionName) || section == null)
            section = ConfigUtils.INSTANCE.config.createSection(sectionName);

        final ConfigurationSection finalSection = section;

        this.buttons.forEach((id, button) -> {
            finalSection.set(String.format("%s.place-on", id), button.getPlaceOn());
            finalSection.set(String.format("%s.block-face", id), button.getFace().name());
        });

        ConfigUtils.INSTANCE.save();
        this.setButtons();
    }

    private void setMagicFloors() {
        final ConfigurationSection section = ConfigUtils.INSTANCE.config.getConfigurationSection(String.format("%s.magic-floors", this.name));

        if (section == null)
            return;

        section.getKeys(false).forEach(id -> {
            final Location loc1 = (Location) section.get(String.format("%s.loc-1", id));
            final Location loc2 = (Location) section.get(String.format("%s.loc-2", id));

            final MagicFloor magicFloor = new MagicFloor(loc1, loc2);
            this.magicFloors.put(Integer.valueOf(id), magicFloor);
        });
    }

    /**
     * Add a magic floor to this arena
     * @param magicFloor
     */
    public void addMagicFloor(final MagicFloor magicFloor) {
        final int nextId = this.magicFloors.size();
        this.magicFloors.put(nextId, magicFloor);
    }

    /**
     * Save all the magic floors in the arena to the config
     */
    public void saveMagicFloors() {
        final String sectionName = String.format("%s.magic-floors", this.name);
        ConfigurationSection section = ConfigUtils.INSTANCE.config.getConfigurationSection(sectionName);

        if (ConfigUtils.INSTANCE.config.isConfigurationSection(sectionName) || section == null)
            section = ConfigUtils.INSTANCE.config.createSection(sectionName);

        final ConfigurationSection finalSection = section;

        this.magicFloors.forEach((id, floor) -> {
            finalSection.set(String.format("%s.loc-1", id), floor.getLoc1());
            finalSection.set(String.format("%s.loc-2", id), floor.getLoc2());
        });

        ConfigUtils.INSTANCE.save();
        this.setButtons();
    }

    public Player getPlayer1() {
        return Bukkit.getPlayer(this.uuid1);
    }

    public Player getPlayer2() {
        return Bukkit.getPlayer(this.uuid2);
    }

    /**
     * Returns a list of all the players in this arena
     * @return
     */
    public List<Player> getPlayers() {
        final List<Player> players = new ArrayList<>();

        final Player p1 = this.getPlayer1();
        final Player p2 = this.getPlayer2();

        if (p1 != null)
            players.add(p1);

        if (p2 != null)
            players.add(p2);

        return players;
    }

    /**
     * Checks if any of this arena's players has specified uuid
     * @param uuid
     * @return
     */
    public boolean containsUuid(final UUID uuid) {
        if (this.uuid1 == null || this.uuid2 == null)
            return false;

        return this.uuid1.equals(uuid) || this.uuid2.equals(uuid);
    }

    /**
     * TODO: Change this method to do the slow teleport where it pushes you into the app (Fluyd from the future, hellins brain had a buffer overflow, this is nonsense data)
     */
    protected void teleport() {
        final Player p1 = this.getPlayer1();
        final Player p2 = this.getPlayer2();

        if (p1 != null)
            this.phaseIn(p1, this.getLoc1());

        if (p2 != null)
            this.phaseIn(p2, this.getLoc2());
    }

    protected void sendTitle() {
        this.getPlayers().stream().filter(p -> p != null && p.isOnline()).forEach(p -> p.sendTitle(ChatColor.translateAlternateColorCodes('&', String.format("&a&l%s", this.getName())), ChatColor.translateAlternateColorCodes('&', this.getSubTitle())));
    }

    protected void startMagicFloors() {
        if (!this.getMagicFloors().isEmpty())
            this.getMagicFloors().values().forEach(MagicFloor::start);
    }

    /**
     * Check if the game started longer than x amount of seconds ago
     * @param seconds
     * @return
     */
    protected boolean startedLongerThan(final long seconds) {
        return GameManager.startedLongerThan(seconds);
    }

    /**
     * Gets the other player in the list of players
     * @param p
     * @return
     */
    protected Player getOtherPlayer(final Player p) {
        for (final Player player : this.getPlayers()) {
            if (p == player)
                continue;

            return player;
        }

        return null;
    }

    /**
     * Called when determining who to eliminate
     * Override this is you need your arena to check for extra stuff than what is already checked in the GameManager class
     * @param p
     * @return
     */
    public boolean isEliminated(final Player p) {
        return false;
    }

    /**
     * Phases the player into the app and then teleports them to the apps corresponding location
     */
    private void phaseIn(final Player p, final Location arenaLocation) {
        if (this.viewLoc == null || this.startedLongerThan(10)) {
            p.teleport(arenaLocation);
            return;
        }

        final float originalFlySpeed = p.getFlySpeed();

        p.teleport(this.getViewLoc());
        p.setGameMode(GameMode.SPECTATOR);
        p.setFlySpeed(0F);

        new BukkitRunnable() {
            double speed = 0.2;
            int count = 0;
            float pitch = 1F;

            @Override
            public void run() {
                if (count >= 3) {
                    super.cancel();

                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2 * 20, 0, false, false));
                    p.setFlySpeed(originalFlySpeed);
                    p.teleport(arenaLocation);
                    p.setGameMode(GameMode.SURVIVAL);
                    p.playSound(p.getLocation(), Sound.EXPLODE, 1f, 1f);
                    return;
                }

                for (int i = 0; i < count + 1; i++)
                    p.playSound(p.getLocation(), Sound.NOTE_PLING, 2f, pitch);

                pitch += 0.1F;

                final Location loc = p.getLocation();
                loc.setYaw(viewLoc.getYaw());
                loc.setPitch(viewLoc.getPitch());
                p.teleport(loc);

                Vector velocity = p.getLocation().getDirection().multiply(speed);
                p.setVelocity(velocity);

                speed += PHASE_SPEED_INCREMENT;
                count++;
            }
        }.runTaskTimer(AppWars.INSTANCE, 0, PHASE_DURATION);
    }
}