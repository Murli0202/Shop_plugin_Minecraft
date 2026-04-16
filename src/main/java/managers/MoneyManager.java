package com.example.shop.managers;

import com.example.shop.ShopPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoneyManager {

    private final ShopPlugin plugin;
    private Economy vaultEconomy = null;

    // Fallback internal storage (used if Vault is not installed)
    private final Map<UUID, Double> balances = new HashMap<>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public MoneyManager(ShopPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "money.yml");

        if (setupVault()) {
            plugin.getLogger().info("[ShopPlugin] Vault economy found! Using " +
                    vaultEconomy.getName() + " for money.");
        } else {
            plugin.getLogger().warning("[ShopPlugin] Vault not found! Using internal money storage.");
            loadData();
        }
    }

    private boolean setupVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp =
                Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }

    public boolean isVaultEnabled() { return vaultEconomy != null; }

    public double getBalance(UUID uuid) {
        if (vaultEconomy != null) {
            return vaultEconomy.getBalance(Bukkit.getOfflinePlayer(uuid));
        }
        return balances.getOrDefault(uuid, plugin.getConfig().getDouble("starting-balance", 500.0));
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (vaultEconomy != null) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (!vaultEconomy.has(player, amount)) return false;
            vaultEconomy.withdrawPlayer(player, amount);
            return true;
        }
        if (getBalance(uuid) < amount) return false;
        balances.put(uuid, getBalance(uuid) - amount);
        return true;
    }

    public void deposit(UUID uuid, double amount) {
        if (vaultEconomy != null) {
            vaultEconomy.depositPlayer(Bukkit.getOfflinePlayer(uuid), amount);
            return;
        }
        balances.put(uuid, getBalance(uuid) + amount);
    }

    public String format(double amount) {
        if (vaultEconomy != null) return vaultEconomy.format(amount);
        String symbol = plugin.getConfig().getString("money-symbol", "$");
        return symbol + String.format("%.2f", amount);
    }

    public void loadData() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException e) {
                plugin.getLogger().severe("Could not create money.yml"); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (dataConfig.contains("balances")) {
            for (String key : dataConfig.getConfigurationSection("balances").getKeys(false)) {
                try { balances.put(UUID.fromString(key), dataConfig.getDouble("balances." + key)); }
                catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void saveData() {
        if (vaultEconomy != null) return;
        balances.forEach((uuid, amount) -> dataConfig.set("balances." + uuid, amount));
        try { dataConfig.save(dataFile); } catch (IOException e) {
            plugin.getLogger().severe("Could not save money.yml"); }
    }
}
