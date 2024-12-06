package org.losttribe.leverPuzzle;

import com.losttribe.leverpuzzle.LeverPuzzle;
import com.losttribe.leverpuzzle.managers.LeverManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LeverCommand implements CommandExecutor {

    private final LeverPuzzle plugin;
    private final LeverManager leverManager;

    public LeverCommand(LeverPuzzle plugin, LeverManager leverManager) {
        this.plugin = plugin;
        this.leverManager = leverManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lt.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "setup":
                handleSetup(player);
                break;
            case "setwall":
                handleSetWall(player, args);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand.");
                sendUsage(player);
                break;
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Usage: /levers <setup|setwall>");
    }

    private void handleSetup(Player player) {
        if (leverManager.isSettingUp(player)) {
            leverManager.finishSetup(player);
            player.sendMessage(ChatColor.GREEN + "Lever setup disabled.");
        } else {
            leverManager.startSetup(player);
            player.sendMessage(ChatColor.GREEN + "Lever setup enabled. Left-click levers to select.");
        }
    }

    private void handleSetWall(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /levers setwall <top|bottom>");
            return;
        }

        String position = args[1].toLowerCase();
        Location loc = player.getLocation();

        switch (position) {
            case "top":
                leverManager.setWallCorner(player, loc, true);
                player.sendMessage(ChatColor.GREEN + "Top left corner of the door set.");
                break;
            case "bottom":
                leverManager.setWallCorner(player, loc, false);
                player.sendMessage(ChatColor.GREEN + "Bottom right corner of the door set.");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid argument. Use 'top' or 'bottom'.");
                break;
        }
    }
}
