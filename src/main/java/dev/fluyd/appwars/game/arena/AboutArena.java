package dev.fluyd.appwars.game.arena;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AboutArena {
    String name();
    boolean pvp() default false;
    boolean build() default false;
    boolean allowInteraction() default false;
    boolean allowDropItems() default false;
    boolean noFall() default true;
    String subTitle() default "&eBe the last one standing against your opponent!";
    boolean available() default true;
}