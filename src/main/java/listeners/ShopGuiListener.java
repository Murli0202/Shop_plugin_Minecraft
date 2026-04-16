package com.example.shop.listeners;

import com.example.shop.ShopPlugin;
import com.example.shop.gui.GuiManager;
import com.example.shop.model.ShopCategory;
import com.example.shop.model.ShopItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShopGuiListener implements Listener {

    private final ShopPlugin plugin;
    private final GuiManager guiManager;

    public ShopGuiListener(ShopPlugin plugin) {
        this.plugin = plugin;
        this.guiManager = new GuiManager(plugin);
    }

    public GuiManager getGuiManager() { return guiManager; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.startsWith(GuiManager.MAIN_TITLE_PREFIX) &&
            !title.startsWith(GuiManager.CATEGORY_TITLE_PREFIX)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR
                || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // ── Main menu click ───────────────────────────────────────
        if (title.equals(GuiManager.MAIN_TITLE_PREFIX)) {
            int slot = event.getSlot();
            List<ShopCategory> cats = new ArrayList<>(plugin.getShopManager().getCategories().values());
            if (slot < cats.size()) {
                guiManager.openCategoryMenu(player, cats.get(slot));
            }
            return;
        }

        // ── Category menu click ───────────────────────────────────
        if (title.startsWith(GuiManager.CATEGORY_TITLE_PREFIX)) {
            // Back button
            if (clicked.getType() == Material.ARROW) {
                guiManager.openMainMenu(player);
                return;
            }

            // Find category from title
            String catDisplayName = title.replace(GuiManager.CATEGORY_TITLE_PREFIX, "");
            ShopCategory category = plugin.getShopManager().getCategories().values().stream()
                    .filter(c -> c.getDisplayName().equals(catDisplayName))
                    .findFirst().orElse(null);

            if (category == null) return;

            int slot = event.getSlot();
            List<ShopItem> items = category.getItems();
            if (slot >= items.size()) return;

            ShopItem shopItem = items.get(slot);
            handlePurchase(player, shopItem, category);
        }
    }

    private void handlePurchase(Player player, ShopItem shopItem, ShopCategory category) {
        boolean isOp = player.isOp();

        if (!isOp) {
            double price = shopItem.getPrice();
            double balance = plugin.getMoneyManager().getBalance(player.getUniqueId());

            if (balance < price) {
                player.sendMessage("§c§lShop §r§cYou don't have enough money!");
                player.sendMessage("§7Price: §f" + plugin.getMoneyManager().format(price)
                        + " §7| Balance: §f" + plugin.getMoneyManager().format(balance));
                return;
            }
            plugin.getMoneyManager().withdraw(player.getUniqueId(), price);
        }

        // Give items
        ItemStack reward = new ItemStack(shopItem.getMaterial(), shopItem.getAmount());
        player.getInventory().addItem(reward).forEach((k, v) ->
                player.getWorld().dropItemNaturally(player.getLocation(), v));

        String priceMsg = isOp ? "§afree §7(OP)" : "§f" + plugin.getMoneyManager().format(shopItem.getPrice());
        player.sendMessage("§a§lShop §r§aYou bought §f" + shopItem.getAmount()
                + "x " + shopItem.getName() + " §afor " + priceMsg + "§a!");

        // Refresh category menu to update balance shown
        guiManager.openCategoryMenu(player, category);
    }
}
