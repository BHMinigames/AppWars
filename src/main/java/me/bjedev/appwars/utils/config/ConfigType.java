package me.bjedev.appwars.utils.config;

import lombok.Getter;

@Getter
public enum ConfigType {
    MIN_PLAYERS(6),
    MAX_PLAYERS(12),
    LOBBY_LOCATION,
    ROUND_LENGTH(60);

    private final String name = pretty(this.name());
    private final Object defaultValue;

    ConfigType() {
        this(null);
    }

    ConfigType(final Object obj) {
        this.defaultValue = obj;
    }

    public void set(final Object value) {
        ConfigUtils.INSTANCE.config.set(this.getName(), value);
        ConfigUtils.INSTANCE.save();
    }

    public static String pretty(final String str) {
        return str.toLowerCase().replace("_", "-");
    }
}