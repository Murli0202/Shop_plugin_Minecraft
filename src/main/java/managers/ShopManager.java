package com.example.shop.managers;

import com.example.shop.ShopPlugin;
import com.example.shop.model.ShopCategory;
import com.example.shop.model.ShopItem;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShopManager {

    private final ShopPlugin plugin;
    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();

    public ShopManager(ShopPlugin plugin) {
        this.plugin = plugin;
        loadCategories();
    }

    private void loadCategories() {
        categories.clear();
        if (!plugin.getConfig().contains("categories")) {
            plugin.getLogger().warning("No categories found in config.yml!");
            return;
        }

        for (String catId : plugin.getConfig().getConfigurationSection("categories").getKeys(false)) {
            String path = "categories." + catId;
            String displayName = color(plugin.getConfig().getString(path + ".display-name", catId));
            String iconStr = plugin.getConfig().getString(path + ".icon", "CHEST");
            Material icon = parseMaterial(iconStr, Material.CHEST);

            List<ShopItem> items = new ArrayList<>();
            if (plugin.getConfig().contains(path + ".items")) {
                for (String itemKey : plugin.getConfig().getConfigurationSection(path + ".items").getKeys(false)) {
                    String iPath = path + ".items." + itemKey;
                    String itemName = color(plugin.getConfig().getString(iPath + ".name", itemKey));
                    String matStr = plugin.getConfig().getString(iPath + ".material", "STONE");
                    Material mat = parseMaterial(matStr, null);
                    if (mat == null) continue;
                    double price = plugin.getConfig().getDouble(iPath + ".price", 10.0);
                    int amount = plugin.getConfig().getInt(iPath + ".amount", 1);
                    items.add(new ShopItem(itemName, mat, price, amount));
                }
            }
            categories.put(catId, new ShopCategory(catId, displayName, icon, items));
        }
        plugin.getLogger().info("Loaded " + categories.size() + " shop categories.");
    }

    public Map<String, ShopCategory> getCategories() { return categories; }

    public ShopCategory getCategory(String id) { return categories.get(id); }

    private Material parseMaterial(String str, Material fallback) {
        try { return Material.valueOf(str.toUpperCase()); }
        catch (IllegalArgumentException e) { return fallback; }
    }

    private String color(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
}
