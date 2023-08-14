package dev.fluyd.appwars.utils;

import dev.fluyd.appwars.AppWars;
import org.apache.commons.io.FileUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class ConfigUtils {
    private final File configFile;
    public final YamlConfiguration config;
    private final ConfigurationSection settings;

    public Location lobbyLocation;

    public ConfigUtils() {
        configFile = new File(AppWars.INSTANCE.getDataFolder(), "config.yml");
        config = getConfig();
        settings = config != null ? config.getConfigurationSection("settings") : null;

        if (settings == null)
            return;
    }

    private void setValues() {
        this.lobbyLocation = (Location) settings.get("lobby-location");
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

        this.setValues();
    }

    private YamlConfiguration getConfig() {
        try {
            final boolean alreadyExists = configFile.exists() && configFile.length() != 0;

            if (!alreadyExists) {
                final InputStream resource = ConfigUtils.class.getClassLoader().getResourceAsStream("config.yml");
                if (resource == null)
                    throw new IOException("resource was null");

                FileUtils.copyInputStreamToFile(resource, configFile);
                resource.close();
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
            return null;
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }
}
