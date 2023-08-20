package me.bjedev.appwars;

import me.bjedev.appwars.commands.impl.*;
import me.bjedev.appwars.game.GameManager;
import me.bjedev.appwars.listeners.InitListeners;
import me.bjedev.appwars.listeners.KillTrackerListener;
import me.bjedev.appwars.listeners.PlayerListener;
import me.bjedev.appwars.listeners.WorldListener;
import me.bjedev.appwars.utils.config.ConfigUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class AppWars extends JavaPlugin {
    public static JavaPlugin INSTANCE;
//    public CoreProvider coreProvider;

    @Override
    public void onEnable() {
        INSTANCE = this;

        final InitListeners initListeners = new InitListeners();
        this.registerListener(initListeners);

        initListeners.onWorldLoad = () -> {
            GameManager.initArenas();

//            coreProvider = new CoreProvider("APP_WARS", 12, true, false, this);

            registerListeners();
            registerCommands();
            registerTabCompleters();

            GameManager.roundLength = ConfigUtils.INSTANCE.roundLength;
            GameManager.roundLengthTicks = GameManager.roundLength * 20L;
        };

        Bukkit.getScheduler().runTaskLater(this, () -> CitizensAPI.getNPCRegistry().despawnNPCs(DespawnReason.REMOVAL), 50L); // Delete all Citizens NPC's
    }

    @Override
    public void onDisable() {
        GameManager.disable();
    }

    private void registerListeners() {
        this.registerListener(new PlayerListener());
        this.registerListener(new WorldListener());
        this.registerListener(new KillTrackerListener());
    }

    private void registerListener(final Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    private void registerCommands() {
        Bukkit.getPluginCommand("buildmode").setExecutor(new BuildMode());
        Bukkit.getPluginCommand("test").setExecutor(new Test());
        Bukkit.getPluginCommand("setspawn").setExecutor(new SetSpawn());
        Bukkit.getPluginCommand("spawn").setExecutor(new Spawn());
        Bukkit.getPluginCommand("start").setExecutor(new Start());
        Bukkit.getPluginCommand("loc").setExecutor(new Loc());
        Bukkit.getPluginCommand("addbutton").setExecutor(new AddButton());
        Bukkit.getPluginCommand("magicfloor").setExecutor(new MagicFloor());
        Bukkit.getPluginCommand("wtfthebutton").setExecutor(new WhereIsTheButton());
        Bukkit.getPluginCommand("cancel").setExecutor(new Cancel());
    }

    private void registerTabCompleters() {
        Bukkit.getPluginCommand("test").setTabCompleter(new Test());
        Bukkit.getPluginCommand("loc").setTabCompleter(new Loc());
        Bukkit.getPluginCommand("addbutton").setTabCompleter(new AddButton());
        Bukkit.getPluginCommand("magicfloor").setTabCompleter(new MagicFloor());
    }

    /**
     * Returns true if server version is newer
     * @return
     */
    public static boolean isNewApi() {
        final String version = Bukkit.getVersion().toLowerCase();
        final String[] oldAPI = { "1.7", "1.8", "1.9", "1.10", "1.11", "1.12" };
        for (final String ver : oldAPI)
            if (version.contains(ver))
                return false;

        return true;
    }
}