package org.losttribe.leverPuzzle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class LeverManager {

    private final LeverPuzzle plugin;
    private YamlConfiguration gameData;
    private final Set<Player> setupPlayers; // Tracks players in setup mode

    public LeverManager(LeverPuzzle plugin) {
        this.plugin = plugin;
        this.setupPlayers = new HashSet<>();
        this.gameData = loadGameData();
    }

    /**
     * Loads the game data from 'game_data.yml'.
     * If the file doesn't exist, it creates a new one from the default resource.
     *
     * @return YamlConfiguration object representing the game data.
     */
    public YamlConfiguration loadGameData() {
        File file = new File(plugin.getDataFolder(), "game_data.yml");
        if (!file.exists()) {
            plugin.getLogger().info("No game_data.yml found, creating a new one.");
            plugin.saveResource("game_data.yml", false); // Copies from JAR to data folder
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Saves the current game data to 'game_data.yml'.
     */
    public void saveConfigurations() {
        File file = new File(plugin.getDataFolder(), "game_data.yml");
        try {
            gameData.save(file);
            plugin.getLogger().info("Game data saved successfully.");
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving game data to disk.");
            e.printStackTrace();
        }
    }

    /**
     * Adds a lever to the puzzle with its current state.
     *
     * @param location Location of the lever.
     * @param state    Desired state of the lever (true for powered, false otherwise).
     */
    public void addLever(Location location, boolean state) {
        String path = "levers." + location.getWorld().getName() + "." +
                location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        gameData.set(path + ".state", state);
        saveConfigurations();
        plugin.getLogger().info("Lever added at " + location.toString() + " with state " + state);
    }

    /**
     * Checks if a player is currently in setup mode.
     *
     * @param player The player to check.
     * @return True if the player is in setup mode, false otherwise.
     */
    public boolean isSettingUp(Player player) {
        return setupPlayers.contains(player);
    }

    /**
     * Starts setup mode for a player.
     *
     * @param player The player to add to setup mode.
     */
    public void startSetup(Player player) {
        setupPlayers.add(player);
        plugin.getLogger().info(player.getName() + " has entered setup mode.");
    }

    /**
     * Finishes setup mode for a player.
     *
     * @param player The player to remove from setup mode.
     */
    public void finishSetup(Player player) {
        setupPlayers.remove(player);
        plugin.getLogger().info(player.getName() + " has exited setup mode.");
    }

    /**
     * Sets a wall corner (top-left or bottom-right) based on player input.
     *
     * @param player The player setting the wall corner.
     * @param loc    The location of the corner.
     * @param isTop  True if setting the top-left corner, false for bottom-right.
     */
    public void setWallCorner(Player player, Location loc, boolean isTop) {
        if (isTop) {
            gameData.set("wall.topLeft", loc);
            plugin.getLogger().info(player.getName() + " set wall top-left corner at " + loc.toString());
        } else {
            gameData.set("wall.bottomRight", loc);
            plugin.getLogger().info(player.getName() + " set wall bottom-right corner at " + loc.toString());
        }
        saveConfigurations();
    }

    /**
     * Checks the states of all levers to determine if a specific pattern is achieved.
     * If the pattern matches, performs actions like removing the wall.
     */
    public void checkLeverStates() {
        plugin.getLogger().info("Checking lever states...");

        // Example logic: If all levers are powered, remove the wall
        boolean allLeversPowered = true;

        if (gameData.contains("levers")) {
            for (String world : gameData.getConfigurationSection("levers").getKeys(false)) {
                for (String leverKey : gameData.getConfigurationSection("levers." + world).getKeys(false)) {
                    boolean state = gameData.getBoolean("levers." + world + "." + leverKey + ".state");
                    if (!state) {
                        allLeversPowered = false;
                        break;
                    }
                }
                if (!allLeversPowered) break;
            }
        } else {
            plugin.getLogger().warning("No levers configured in game_data.yml.");
            return;
        }

        if (allLeversPowered) {
            removeWall();
            Bukkit.broadcastMessage(ChatColor.GREEN + "The wall has been removed! Access granted.");
            plugin.getLogger().info("All levers are powered. Wall removed.");
        } else {
            plugin.getLogger().info("Lever pattern not matched. Wall remains.");
        }
    }

    /**
     * Removes the wall based on the configured top-left and bottom-right corners.
     */
    private void removeWall() {
        plugin.getLogger().info("Removing the wall...");

        Location topLeft = gameData.getLocation("wall.topLeft");
        Location bottomRight = gameData.getLocation("wall.bottomRight");

        if (topLeft == null || bottomRight == null) {
            plugin.getLogger().severe("Wall coordinates are not set in game_data.yml.");
            return;
        }

        int minX = Math.min(topLeft.getBlockX(), bottomRight.getBlockX());
        int maxX = Math.max(topLeft.getBlockX(), bottomRight.getBlockX());

        int minY = Math.min(topLeft.getBlockY(), bottomRight.getBlockY());
        int maxY = Math.max(topLeft.getBlockY(), bottomRight.getBlockY());

        int minZ = Math.min(topLeft.getBlockZ(), bottomRight.getBlockZ());
        int maxZ = Math.max(topLeft.getBlockZ(), bottomRight.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(topLeft.getWorld(), x, y, z);
                    loc.getBlock().setType(Material.AIR);
                }
            }
        }

        plugin.getLogger().info("Wall removed between " + topLeft.toString() + " and " + bottomRight.toString());
    }
}
