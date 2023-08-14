package dev.fluyd.appwars;

import dev.fluyd.appwars.commands.BuildModeCommand;
import dev.fluyd.appwars.commands.TestCommand;
import dev.fluyd.appwars.listeners.PlayerListener;
import dev.fluyd.appwars.listeners.WorldListener;
import dev.fluyd.spigotcore.CoreProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AppWars extends JavaPlugin {
    public static JavaPlugin INSTANCE;
    public CoreProvider coreProvider;

    @Override
    public void onEnable() {
        INSTANCE = this;

        Bukkit.getScheduler().runTaskLater(this, () -> {
            coreProvider = new CoreProvider("APP_WARS", 12, true, false, this);
        }, 20L);

        registerListeners();
        registerCommands();
        registerTabCompleters();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);
    }

    private void registerCommands() {
        Bukkit.getPluginCommand("buildmode").setExecutor(new BuildModeCommand());
        Bukkit.getPluginCommand("test").setExecutor(new TestCommand());
    }

    private void registerTabCompleters() {
        Bukkit.getPluginCommand("test").setTabCompleter(new TestCommand());
    }
}