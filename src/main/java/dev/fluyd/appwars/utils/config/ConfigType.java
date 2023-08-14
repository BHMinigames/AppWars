package dev.fluyd.appwars.utils.config;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors
public enum ConfigType {
    LOBBY_LOCATION;

    private final String name;

    ConfigType() {
        this.name = this.name().toLowerCase().replace("_", "-");
    }

    public void set(final Object value) {
        ConfigUtils.INSTANCE.config.set(this.getName(), value);
        ConfigUtils.INSTANCE.save();
    }
}