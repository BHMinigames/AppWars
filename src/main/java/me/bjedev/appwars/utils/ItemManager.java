package me.bjedev.appwars.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

@UtilityClass
public final class ItemManager {
    private final ItemStack[] twitterKitHotbar = {
            createItem(Material.STONE_SWORD, null, 0, 1),
            createItem(Material.GOLDEN_APPLE, null, 0, 5),
            createItem(Material.STAINED_CLAY, null, 0, 32),
            createItem(Material.GOLD_PICKAXE, Enchantment.DIG_SPEED, 5, 1)
    };

    public static void giveTwitterKit(final Player p) {
        final PlayerInventory inv = p.getInventory();

        for (int i = 0; i < twitterKitHotbar.length; ++i) {
            final ItemStack item = twitterKitHotbar[i];
            inv.setItem(i, item);
        }

        inv.setHelmet(createItem(Material.IRON_HELMET, Enchantment.PROTECTION_ENVIRONMENTAL, 1, 1));
        inv.setChestplate(createItem(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION_ENVIRONMENTAL, 2, 1));
        inv.setLeggings(createItem(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION_ENVIRONMENTAL, 2, 1));
        inv.setBoots(createItem(Material.IRON_BOOTS, Enchantment.PROTECTION_ENVIRONMENTAL, 1, 1));

        p.updateInventory();
    }

    private static ItemStack createItem(final Material material, final Enchantment enchant, final int enchantLevel, final int amount) {
        final ItemStack item = new ItemStack(material, amount);
        final ItemMeta meta = item.getItemMeta();

        if (enchant != null)
            meta.addEnchant(enchant, enchantLevel, true);

        item.setItemMeta(meta);
        return item;
    }
}