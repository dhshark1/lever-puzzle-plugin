package org.losttribe.leverPuzzle;

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

        // If the clicked block is null, return
        if (clickedBlock == null) return;

        // Handle setup mode: left-click on a lever to add it
        if (leverManager.isSettingUp(player) && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleSetupClick(player, clickedBlock, event);
            return;
        }

        // Handle lever state changes: right-click to toggle the lever
        if (clickedBlock.getType() == Material.LEVER && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleLeverToggle(player, clickedBlock);
        }
    }

    private void handleSetupClick(Player player, Block block, PlayerInteractEvent event) {
        // Ensure the block clicked is a lever
        if (block.getType() != Material.LEVER) {
            player.sendMessage(ChatColor.RED + "Please left-click on a lever.");
            return;
        }

        // Get the lever state (whether it's powered or not)
        boolean isPowered = block.getBlockData().getAsString().contains("powered=true");

        // Add the lever to the puzzle (using the LeverManager)
        leverManager.addLever(block.getLocation(), isPowered);
        player.sendMessage(ChatColor.GREEN + "Lever added to the puzzle.");

        // Cancel the event to prevent further interaction
        event.setCancelled(true);
    }

    private void handleLeverToggle(Player player, Block block) {
        // Check if the block is indeed a lever
        if (block.getType() != Material.LEVER) return;

        // Check the current state of the lever (powered or not)
        boolean isPowered = block.getBlockData().getAsString().contains("powered=true");

        // Update the lever state in the LeverManager
        leverManager.addLever(block.getLocation(), isPowered);

        // Provide feedback to the player
        String stateMessage = isPowered ? "Lever turned ON." : "Lever turned OFF.";
        player.sendMessage(ChatColor.YELLOW + stateMessage);

        // Optionally, update lever states after a short delay
        plugin.getServer().getScheduler().runTaskLater(plugin, leverManager::checkLeverStates, 1L);
    }
}
