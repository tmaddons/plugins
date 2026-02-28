package com.clearbin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
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
    private BukkitTask ramCheckTask;
    private int secondsRemaining;
    private boolean forceClearing = false;
    private boolean highRamMode = false;
    private int currentInterval;
    
    public AutoClearManager(ClearBin plugin) {
        this.plugin = plugin;
    }
    
    public void start() {
        stop();
        
        if (!plugin.getConfig().getBoolean("settings.auto-clear", true)) {
            return;
        }
        
        currentInterval = plugin.getConfig().getInt("settings.interval-seconds", 600);
        secondsRemaining = currentInterval;
        
        // Main clear task
        mainTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            clearItems();
            secondsRemaining = currentInterval;
        }, currentInterval * 20L, currentInterval * 20L);
        
        // Warning task (runs every second)
        if (plugin.getConfig().getBoolean("warnings.enabled", true)) {
            warningTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                secondsRemaining--;
                
                List<Integer> warningTimes = plugin.getConfig().getIntegerList("warnings.times");
                if (warningTimes.contains(secondsRemaining)) {
                    sendWarning(secondsRemaining);
                }
                
                if (secondsRemaining <= 0) {
                    secondsRemaining = currentInterval;
                }
            }, 20L, 20L);
        }
        
        // High RAM monitoring task
        if (plugin.getConfig().getBoolean("settings.high-ram-mode.enabled", true)) {
            int checkInterval = plugin.getConfig().getInt("settings.high-ram-mode.check-interval-seconds", 60);
            ramCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkRamUsage, 
                checkInterval * 20L, checkInterval * 20L);
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
        if (ramCheckTask != null) {
            ramCheckTask.cancel();
            ramCheckTask = null;
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
        showRandomSuggestion();
        
        return count;
    }
    
    private void sendWarning(int seconds) {
        String message = plugin.getConfig().getString("messages.prefix", "") +
                        plugin.getConfig().getString("messages.warning", "");
        message = Utils.colorize(message.replace("%time%", String.valueOf(seconds)));
        
        Bukkit.broadcastMessage(message);
    }
    
    public void forceClear(CommandSender sender) {
        if (forceClearing) {
            sender.sendMessage(Utils.colorize(plugin.getConfig().getString("messages.prefix", "") + 
                "&cForce clear already in progress!"));
            return;
        }
        
        forceClearing = true;
        sender.sendMessage(Utils.colorize(plugin.getConfig().getString("messages.prefix", "") + 
            "&eForce clearing items in 15 seconds..."));
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // 15 second warning
            Bukkit.broadcastMessage(Utils.colorize(plugin.getConfig().getString("messages.prefix", "") + 
                "&cForce clear: Items will be removed in 15 seconds!"));
        }, 0L);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // 10 second warning
            Bukkit.broadcastMessage(Utils.colorize(plugin.getConfig().getString("messages.prefix", "") + 
                "&cForce clear: Items will be removed in 10 seconds!"));
        }, 100L);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // 5 second warning
            Bukkit.broadcastMessage(Utils.colorize(plugin.getConfig().getString("messages.prefix", "") + 
                "&cForce clear: Items will be removed in 5 seconds!"));
        }, 200L);
        
        // Countdown 3-2-1
        for (int i = 3; i >= 1; i--) {
            final int count = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.broadcastMessage(Utils.colorize(plugin.getConfig().getString("messages.prefix", "") + 
                    "&c" + count + "..."));
            }, (240L + (3 - count) * 20L));
        }
        
        // Clear after 15 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int cleared = clearItems();
            String message = plugin.getConfig().getString("messages.prefix", "") +
                           plugin.getConfig().getString("messages.force-clear", "");
            Bukkit.broadcastMessage(Utils.colorize(message.replace("%items%", String.valueOf(cleared))));
            forceClearing = false;
        }, 300L);
    }
    
    private void showRandomSuggestion() {
        List<String> suggestions = plugin.getConfig().getStringList("suggestions");
        if (suggestions != null && !suggestions.isEmpty()) {
            String suggestion = suggestions.get((int) (Math.random() * suggestions.size()));
            Bukkit.broadcastMessage(Utils.colorize(suggestion));
        }
    }
    
    private void checkRamUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        int usagePercent = (int) ((usedMemory * 100) / maxMemory);
        
        int threshold = plugin.getConfig().getInt("settings.high-ram-mode.ram-threshold-percent", 80);
        int normalInterval = plugin.getConfig().getInt("settings.interval-seconds", 600);
        int fastInterval = plugin.getConfig().getInt("settings.high-ram-mode.fast-interval-seconds", 300);
        
        if (usagePercent >= threshold && !highRamMode) {
            // Switch to high RAM mode (5 min)
            highRamMode = true;
            currentInterval = fastInterval;
            
            String message = plugin.getConfig().getString("messages.prefix", "") +
                           plugin.getConfig().getString("messages.high-ram-mode-enabled", "");
            message = Utils.colorize(message.replace("%ram%", String.valueOf(usagePercent)));
            Bukkit.broadcastMessage(message);
            
            // Restart tasks with new interval
            restart();
            
        } else if (usagePercent < threshold && highRamMode) {
            // Switch back to normal mode (10 min)
            highRamMode = false;
            currentInterval = normalInterval;
            
            String message = plugin.getConfig().getString("messages.prefix", "") +
                           plugin.getConfig().getString("messages.high-ram-mode-disabled", "");
            message = Utils.colorize(message.replace("%ram%", String.valueOf(usagePercent)));
            Bukkit.broadcastMessage(message);
            
            // Restart tasks with new interval
            restart();
        }
    }
    
    private void restart() {
        // Cancel only main and warning tasks, keep RAM check running
        if (mainTask != null) {
            mainTask.cancel();
        }
        if (warningTask != null) {
            warningTask.cancel();
        }
        
        secondsRemaining = currentInterval;
        
        // Restart main clear task
        mainTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            clearItems();
            secondsRemaining = currentInterval;
        }, currentInterval * 20L, currentInterval * 20L);
        
        // Restart warning task
        if (plugin.getConfig().getBoolean("warnings.enabled", true)) {
            warningTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                secondsRemaining--;
                
                List<Integer> warningTimes = plugin.getConfig().getIntegerList("warnings.times");
                if (warningTimes.contains(secondsRemaining)) {
                    sendWarning(secondsRemaining);
                }
                
                if (secondsRemaining <= 0) {
                    secondsRemaining = currentInterval;
                }
            }, 20L, 20L);
        }
    }
    
    public int getSecondsRemaining() {
        return secondsRemaining;
    }
}
