package com.example.shop;

import com.example.shop.commands.ShopCommand;
import com.example.shop.listeners.ShopGuiListener;
import com.example.shop.managers.MoneyManager;
import com.example.shop.managers.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {

    private MoneyManager moneyManager;
    private ShopManager shopManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        moneyManager = new MoneyManager(this);
        shopManager = new ShopManager(this);

        getServer().getPluginManager().registerEvents(new ShopGuiListener(this), this);
        getCommand("shop").setExecutor(new ShopCommand(this));

        getLogger().info("ShopPlugin enabled!");
    }

    @Override
    public void onDisable() {
        if (moneyManager != null) moneyManager.saveData();
        getLogger().info("ShopPlugin disabled.");
    }

    public MoneyManager getMoneyManager() { return moneyManager; }
    public ShopManager getShopManager() { return shopManager; }
}
