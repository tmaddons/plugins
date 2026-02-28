package com.clearbin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class AutoClearManager {
    
    private final ClearBin plugin;
    private BukkitTask mainTask;
    private BukkitTask warningTask;
    private int secondsRemaining;
    
    public AutoClearManager(ClearBin plugin) {
        this.plugin = plugin;
    }
    
    public void start() {
        stop();
        
        if (!plugin.getConfig().getBoolean("settings.auto-clear", true)) {
            return;
        }
        
        int interval = plugin.getConfig().getInt("settings.interval-seconds", 600);
        secondsRemaining = interval;
        
        // Main clear task
        mainTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            clearItems();
            secondsRemaining = interval;
        }, interval * 20L, interval * 20L);
        
        // Warning task (runs every second)
        if (plugin.getConfig().getBoolean("warnings.enabled", true)) {
            warningTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                secondsRemaining--;
                
                List<Integer> warningTimes = plugin.getConfig().getIntegerList("warnings.times");
                if (warningTimes.contains(secondsRemaining)) {
                    sendWarning(secondsRemaining);
                }
                
                if (secondsRemaining <= 0) {
                    secondsRemaining = interval;
                }
            }, 20L, 20L);
        }
    }
    
    public void stop() {
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        if (warningTask != null) {
            warningTask.cancel();
            warningTask = null;
        }
    }
    
    public void reload() {
        start();
    }
    
    public int clearItems() {
        int count = 0;
        boolean clearArrows = plugin.getConfig().getBoolean("settings.clear-arrows", false);
        boolean clearXp = plugin.getConfig().getBoolean("settings.clear-xp", false);
        
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    Item item = (Item) entity;
                    if (item.getOwner() != null) {
                        Player owner = Bukkit.getPlayer(item.getOwner());
                        if (owner != null && owner.hasPermission("clearbin.bypass")) {
                            continue;
                        }
                    }
                    entity.remove();
                    count++;
                } else if (clearArrows && entity instanceof Projectile) {
                    entity.remove();
                    count++;
                } else if (clearXp && entity instanceof ExperienceOrb) {
                    entity.remove();
                    count++;
                }
            }
        }
        
        String message = plugin.getConfig().getString("messages.prefix", "") +
                        plugin.getConfig().getString("messages.cleared", "");
        message = Utils.colorize(message.replace("%items%", String.valueOf(count)));
        
        Bukkit.broadcastMessage(message);
        
        return count;
    }
    
    private void sendWarning(int seconds) {
        String message = plugin.getConfig().getString("messages.prefix", "") +
                        plugin.getConfig().getString("messages.warning", "");
        message = Utils.colorize(message.replace("%time%", String.valueOf(seconds)));
        
        Bukkit.broadcastMessage(message);
    }
    
    public int getSecondsRemaining() {
        return secondsRemaining;
    }
}
