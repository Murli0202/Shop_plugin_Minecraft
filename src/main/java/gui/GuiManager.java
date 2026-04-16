package com.example.shop.gui;

import com.example.shop.ShopPlugin;
import com.example.shop.model.ShopCategory;
import com.example.shop.model.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiManager {

    private final ShopPlugin plugin;

    // GUI title prefixes for identification
    public static final String MAIN_TITLE_PREFIX = "§6§lShop - Categories";
    public static final String CATEGORY_TITLE_PREFIX = "§6§lShop - ";

    public GuiManager(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Main Category Menu ─────────────────────────────────────────

    public void openMainMenu(Player player) {
        List<ShopCategory> cats = new ArrayList<>(plugin.getShopManager().getCategories().values());
        int size = Math.max(9, (int)(Math.ceil(cats.size() / 9.0)) * 9);
        size = Math.min(size, 54);

        Inventory inv = Bukkit.createInventory(null, size, MAIN_TITLE_PREFIX);

        for (int i = 0; i < cats.size() && i < size; i++) {
            ShopCategory cat = cats.get(i);
            ItemStack icon = makeItem(cat.getIcon(), cat.getDisplayName(),
                    List.of("§7Click to browse", "§7" + cat.getItems().size() + " items available"));
            inv.setItem(i, icon);
        }

        // Fill empty slots with gray glass
        fillEmpty(inv, size);

        player.openInventory(inv);
    }

    // ── Category Item Menu ─────────────────────────────────────────

    public void openCategoryMenu(Player player, ShopCategory category) {
        List<ShopItem> items = category.getItems();
        int size = Math.max(18, (int)(Math.ceil((items.size() + 9) / 9.0)) * 9);
        size = Math.min(size, 54);

        Inventory inv = Bukkit.createInventory(null, size, CATEGORY_TITLE_PREFIX + category.getDisplayName());

        for (int i = 0; i < items.size() && i < size - 9; i++) {
            ShopItem item = items.get(i);
            boolean isOp = player.isOp();
            String priceStr = isOp ? "§aFREE §7(OP)" : "§e" + plugin.getMoneyManager().format(item.getPrice());
            String balance = "§7Your balance: §f" + plugin.getMoneyManager().format(
                    plugin.getMoneyManager().getBalance(player.getUniqueId()));

            ItemStack icon = makeItem(item.getMaterial(), item.getName(),
                    List.of(
                            "§7Amount: §f" + item.getAmount(),
                            "§7Price: " + priceStr,
                            balance,
                            "",
                            "§aClick to buy!"
                    ));
            inv.setItem(i, icon);
        }

        // Back button in last row
        ItemStack back = makeItem(Material.ARROW, "§c« Back to Categories", List.of("§7Click to go back"));
        inv.setItem(size - 5, back);

        fillEmpty(inv, size);
        player.openInventory(inv);
    }

    // ── Helpers ───────────────────────────────────────────────────

    public ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillEmpty(Inventory inv, int size) {
        ItemStack filler = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < size; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, filler);
            }
        }
    }
}
