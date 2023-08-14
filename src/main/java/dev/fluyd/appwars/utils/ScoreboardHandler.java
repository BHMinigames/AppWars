package dev.fluyd.appwars.utils;

import dev.fluyd.appwars.AppWars;
import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.utils.config.ConfigUtils;
import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
public final class ScoreboardHandler {
    private final Player p;

    public ScoreboardHandler(final Player p, final String title) {
        this.p = p;
        Netherboard.instance().createBoard(p, ChatColor.translateAlternateColorCodes('&', title));

        Bukkit.getScheduler().runTaskTimer(AppWars.INSTANCE, this::update, 5L, 20L);
    }

    public void update() {
        if (this.p == null || !this.p.isOnline())
            return;

        final BPlayerBoard board = Netherboard.instance().getBoard(this.p);
        final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");

        board.setAll(String.format("§7%s §8LOCAL", sdf.format(new Date())),
                "§a ",
                "§fMap: §aClassic",
                String.format("§fPlayers: §a%s/%s", Bukkit.getOnlinePlayers().size(), ConfigUtils.INSTANCE.maxPlayers),
                "§e ",
                "§fMode: §aSolo",
                "§fVersion: §7v1.0",
                String.format("§fState: §a%s", GameManager.state.name()),
                "§f ",
                "§erizon.lol");
    }
}