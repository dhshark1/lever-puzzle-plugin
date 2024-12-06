package org.losttribe.leverpuzzle;

import org.losttribe.leverpuzzle.LeverPuzzle;
import org.losttribe.leverpuzzle.managers.LeverManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class LeverListener implements Listener {

    private final LeverPuzzle plugin;
    private final LeverManager leverManager;

    public LeverListener(LeverPuzzle plugin, LeverManager leverManager) {
        this.plugin = plugin;
        this.leverManager = leverManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) return;

        // Handle setup mode
        if (leverManager.isSettingUp(player) && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleSetupClick(player, clickedBlock, event);
            return;
        }

        // Handle lever state changes
        if (clickedBlock.getType() == Material.LEVER && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Schedule a task to check lever states after the state has changed
            plugin.getServer().getScheduler().runTaskLater(plugin, leverManager::checkLeverStates, 1L);
        }
    }

    private void handleSetupClick(Player player, Block block, PlayerInteractEvent event) {
        if (block.getType() != Material.LEVER) {
            player.sendMessage(ChatColor.RED + "Please left-click on a lever.");
            return;
        }

        boolean isPowered = block.getBlockData().getAsString().contains("powered=true");
        leverManager.addLever(player, block.getLocation(), isPowered);
        player.sendMessage(ChatColor.GREEN + "Lever added to the puzzle.");

        event.setCancelled(true);
    }
}
