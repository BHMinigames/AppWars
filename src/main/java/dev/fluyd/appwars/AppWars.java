package dev.fluyd.appwars;

import dev.fluyd.appwars.commands.*;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.listeners.InitListeners;
import dev.fluyd.appwars.listeners.KillTrackerListener;
import dev.fluyd.appwars.listeners.PlayerListener;
import dev.fluyd.appwars.listeners.WorldListener;
import dev.fluyd.appwars.utils.config.ConfigUtils;
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
        Bukkit.getPluginCommand("buildmode").setExecutor(new BuildModeCommand());
        Bukkit.getPluginCommand("test").setExecutor(new TestCommand());
        Bukkit.getPluginCommand("setspawn").setExecutor(new SetSpawn());
        Bukkit.getPluginCommand("spawn").setExecutor(new Spawn());
        Bukkit.getPluginCommand("start").setExecutor(new Start());
        Bukkit.getPluginCommand("loc").setExecutor(new Loc());
    }

    private void registerTabCompleters() {
        Bukkit.getPluginCommand("test").setTabCompleter(new TestCommand());
        Bukkit.getPluginCommand("loc").setTabCompleter(new Loc());
    }
}