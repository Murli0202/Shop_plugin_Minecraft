package com.example.shop.model;

import org.bukkit.Material;

public class ShopItem {
    private final String name;
    private final Material material;
    private final double price;
    private final int amount;

    public ShopItem(String name, Material material, double price, int amount) {
        this.name = name;
        this.material = material;
        this.price = price;
        this.amount = amount;
    }

    public String getName() { return name; }
    public Material getMaterial() { return material; }
    public double getPrice() { return price; }
    public int getAmount() { return amount; }
}
