package me.bjedev.appwars.game.arena;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AboutArena {
    String name(); // Name of arena (Should be all CAPITAL LETTERS)
    boolean pvp() default false; // If true, allows for pvp in arena
    boolean damage() default true; // If false, damage dealt by players is 0
    boolean build() default false; // If true, allows for building and breaking blocks
    boolean allowInteraction() default false; // If true, allows a player to interact with arena
    boolean allowDropItems() default false; // If true, allows a player to drop items
    boolean noFall() default true; // If false, allows a player to take fall damage
    String subTitle() default "&eBe the last one standing against your opponent!"; // Subtitle of the title when first entering the arena
    boolean available() default true; // If false, arena is not part of the random selection for players getting put into arenas
}