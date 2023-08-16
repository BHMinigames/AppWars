package dev.fluyd.appwars.game.arena.impl;

import dev.fluyd.appwars.game.GameManager;
import dev.fluyd.appwars.game.arena.AboutArena;
import dev.fluyd.appwars.game.arena.Arena;
import dev.fluyd.appwars.utils.config.ConfigUtils;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@AboutArena(name = "MAIL", subTitle = "&eWrite more english words than your opponent. Sign the book when your done!")
public final class Mail extends Arena implements Listener {
    private List<String> words;
    private Map<UUID, Integer> wordCountMap = new HashMap<>();

    @Override
    public void start() {
        super.teleport();
        super.sendTitle();

        this.giveBook();
    }

    private void giveBook() {
        this.getPlayers().forEach(p -> p.getInventory().addItem(new ItemStack(Material.BOOK_AND_QUILL)));
    }

    @Override
    public void enable(final JavaPlugin instance) {
        Bukkit.getPluginManager().registerEvents(this, instance);
        this.setWords();
    }

    @Override
    public boolean isEliminated(final Player p) {
        final Map.Entry<UUID, Integer> entry = this.getHighestCount();

        if (entry == null)
            return false;

        return !entry.getKey().equals(p.getUniqueId());
    }

    private Map.Entry<UUID, Integer> getHighestCount() {
        Map.Entry<UUID, Integer> highestEntry = null;

        for (final Map.Entry<UUID, Integer> entry : this.wordCountMap.entrySet()) {
            final int count = entry.getValue();

            if (highestEntry == null || highestEntry.getValue() <= count) {
                if (highestEntry != null && highestEntry.getValue() == count) {
                    highestEntry = null; // Draw
                    continue;
                }

                highestEntry = entry;
            }
        }

        return highestEntry;
    }

    @Override
    public void reset() {
        this.wordCountMap.clear();
    }

    private void setWords() {
        try {
            final File tmp = File.createTempFile("words", "txt");

            final InputStream resource = ConfigUtils.class.getClassLoader().getResourceAsStream("words.txt");
            if (resource == null)
                throw new IOException("words resource was null");

            FileUtils.copyInputStreamToFile(resource, tmp);
            resource.close();

            final Scanner scanner = new Scanner(tmp);
            final List<String> words = new ArrayList<>();

            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                words.add(line.toLowerCase());
            }

            scanner.close();
            this.words = words;
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerEditBook(final PlayerEditBookEvent e) {
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();

        if (!GameManager.players.containsKey(uuid))
            return;

        final Arena arena = GameManager.players.get(uuid);
        if (arena == null || !(arena instanceof Mail))
            return;

        final BookMeta meta = e.getNewBookMeta();
        final String potentialWords = String.join(" ", meta.getPages().toArray(new String[meta.getPageCount()]));

        final int count = this.getEnglishWordCount(potentialWords);
        this.wordCountMap.put(uuid, count);
    }

    private int getEnglishWordCount(String potentialWords) {
        int wordCount = 0;

        potentialWords = potentialWords.trim();
        final String[] splits = potentialWords.split(" ");

        for (String potentialWord : splits) {
            potentialWord = potentialWord.trim();
            if (!this.words.contains(potentialWord.toLowerCase()))
                continue;

            ++wordCount;
        }

        return wordCount;
    }
}