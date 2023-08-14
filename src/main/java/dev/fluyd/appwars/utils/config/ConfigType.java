package dev.fluyd.appwars.utils.config;

import lombok.Getter;

@Getter
public enum ConfigType {
    LOBBY_LOCATION;

    private final String name = this.name().toLowerCase().replace("_", "-");

    public void set(final Object value) {
        ConfigUtils.INSTANCE.config.set(this.getName(), value);
        ConfigUtils.INSTANCE.save();
    }
}