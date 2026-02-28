package com.clearbin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearBinCommand implements CommandExecutor {
    
    private final ClearBin plugin;
    
    public ClearBinCommand(ClearBin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("clearbin.use")) {
                    plugin.getTrashManager().openTrashGUI(player);
                } else {
                    player.sendMessage(Utils.colorize("&cYou don't have permission!"));
                }
            } else {
                sender.sendMessage(Utils.colorize("&cOnly players can open trash GUI!"));
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "open":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("clearbin.use")) {
                        plugin.getTrashManager().openTrashGUI(player);
                    } else {
                        player.sendMessage(Utils.colorize("&cYou don't have permission!"));
                    }
                } else {
                    sender.sendMessage(Utils.colorize("&cOnly players can open trash GUI!"));
                }
                break;
                
            case "clear":
                if (sender.hasPermission("clearbin.clear")) {
                    int count = plugin.getAutoClearManager().clearItems();
                    String message = plugin.getConfig().getString("messages.prefix", "") +
                                   plugin.getConfig().getString("messages.force-clear", "");
                    sender.sendMessage(Utils.colorize(message.replace("%items%", String.valueOf(count))));
                } else {
                    sender.sendMessage(Utils.colorize("&cYou don't have permission!"));
                }
                break;
                
            case "reload":
                if (sender.hasPermission("clearbin.reload")) {
                    plugin.reloadPlugin();
                    String message = plugin.getConfig().getString("messages.prefix", "") +
                                   plugin.getConfig().getString("messages.reload", "");
                    sender.sendMessage(Utils.colorize(message));
                } else {
                    sender.sendMessage(Utils.colorize("&cYou don't have permission!"));
                }
                break;
                
            case "status":
                if (sender.hasPermission("clearbin.status")) {
                    int remaining = plugin.getAutoClearManager().getSecondsRemaining();
                    String message = plugin.getConfig().getString("messages.prefix", "") +
                                   plugin.getConfig().getString("messages.status", "");
                    sender.sendMessage(Utils.colorize(message.replace("%time%", String.valueOf(remaining))));
                } else {
                    sender.sendMessage(Utils.colorize("&cYou don't have permission!"));
                }
                break;
                
            default:
                sender.sendMessage(Utils.colorize("&cUsage: /clearbin [open|clear|reload|status]"));
                break;
        }
        
        return true;
    }
}
