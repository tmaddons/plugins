package com.clearbin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TrashManager {
    
    private final ClearBin plugin;
    
    public TrashManager(ClearBin plugin) {
        this.plugin = plugin;
    }
    
    public void openTrashGUI(Player player) {
        Inventory trash = Bukkit.createInventory(null, 54, Utils.colorize("&8Trash Bin"));
        player.openInventory(trash);
    }
}
