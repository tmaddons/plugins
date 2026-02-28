package com.clearbin;

import org.bukkit.plugin.java.JavaPlugin;

public class ClearBin extends JavaPlugin {
    
    private AutoClearManager autoClearManager;
    private TrashManager trashManager;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        autoClearManager = new AutoClearManager(this);
        trashManager = new TrashManager(this);
        
        getCommand("clearbin").setExecutor(new ClearBinCommand(this));
        
        autoClearManager.start();
        
        getLogger().info("ClearBin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (autoClearManager != null) {
            autoClearManager.stop();
        }
        getLogger().info("ClearBin has been disabled!");
    }
    
    public AutoClearManager getAutoClearManager() {
        return autoClearManager;
    }
    
    public TrashManager getTrashManager() {
        return trashManager;
    }
    
    public void reloadPlugin() {
        reloadConfig();
        autoClearManager.reload();
    }
}
