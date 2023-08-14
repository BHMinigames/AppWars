package dev.fluyd.appwars.utils.config;

import dev.fluyd.appwars.AppWars;
import org.apache.commons.io.FileUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class ConfigUtils {
    public static final ConfigUtils INSTANCE = new ConfigUtils();

    private final File configFile;

    public YamlConfiguration config;

    public Location lobbyLocation;

    public ConfigUtils() {
        this.configFile = new File(AppWars.INSTANCE.getDataFolder(), "config.yml");

        this.setValues();
    }

    private void setValues() {
        this.config = this.getConfig();

        if (config == null)
            throw new RuntimeException("config is null");

        /**
         * Config values
         */
        this.lobbyLocation = (Location) this.config.get(ConfigType.LOBBY_LOCATION.getName());
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
