package com.example.shop.commands;

import com.example.shop.ShopPlugin;
import com.example.shop.gui.GuiManager;
import com.example.shop.listeners.ShopGuiListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private final ShopPlugin plugin;
    private final GuiManager guiManager;

    public ShopCommand(ShopPlugin plugin) {
        this.plugin = plugin;
        this.guiManager = new GuiManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use /shop.");
            return true;
        }
        guiManager.openMainMenu(player);
        return true;
    }
}
