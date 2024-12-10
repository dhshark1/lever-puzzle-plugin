package org.losttribe.leverPuzzle;

import org.bukkit.ChatColor;
import org.bukkit.Location;
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

        if (leverManager.isSettingUp(player) && event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleSetupClick(player, clickedBlock, event);
            return;
        }

        if (clickedBlock.getType() == Material.LEVER && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleLeverToggle(player, clickedBlock);
        }
    }

    private void handleSetupClick(Player player, Block block, PlayerInteractEvent event) {
        if (block.getType() != Material.LEVER) {
            player.sendMessage(ChatColor.RED + "Please left-click on a lever.");
            return;
        }

        boolean isPowered = isLeverPowered(block);

        leverManager.addLever(player, block.getLocation(), isPowered);
        player.sendMessage(ChatColor.GREEN + "Lever added to the puzzle.");

        event.setCancelled(true);
    }

    private void handleLeverToggle(Player player, Block block) {
        if (block.getType() != Material.LEVER) return;

        boolean isPowered = isLeverPowered(block);

        leverManager.addLever(player, block.getLocation(), isPowered);

        String stateMessage = isPowered ? "Lever turned ON." : "Lever turned OFF.";
        player.sendMessage(ChatColor.YELLOW + stateMessage);

        if (leverManager.isLeverPartOfPuzzle(block.getLocation()))  {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> leverManager.checkLeverStates(player), 1L);

        }
    }
    private boolean isLeverPowered(Block block) {
        return block.getBlockData().getAsString().contains("powered=true");
    }

}
