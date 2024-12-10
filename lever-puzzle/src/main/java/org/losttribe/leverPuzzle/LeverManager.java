package org.losttribe.leverPuzzle;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class LeverManager {

    private final LeverPuzzle plugin;
    private YamlConfiguration gameData;
    private final Set<Player> setupPlayers;

    public LeverManager(LeverPuzzle plugin) {
        this.plugin = plugin;
        this.setupPlayers = new HashSet<>();
        this.gameData = loadGameData();
    }

    public YamlConfiguration loadGameData() {
        File file = new File(plugin.getDataFolder(), "game_data.yml");
        if (!file.exists()) {
            plugin.getLogger().info("No game_data.yml found, creating a fresh minimal one.");
            try {
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                if (file.createNewFile()) {
                    String defaultContent =
                            "levers: {}\n" +
                                    "wall:\n" +
                                    "  topLeft: null\n" +
                                    "  bottomRight: null\n";

                    Files.write(file.toPath(), defaultContent.getBytes(StandardCharsets.UTF_8));
                    plugin.getLogger().info("Minimal game_data.yml created successfully.");
                } else {
                    plugin.getLogger().severe("Failed to create game_data.yml. Check file permissions.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Error creating game_data.yml.");
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().info("game_data.yml found and loaded.");
        }

        return YamlConfiguration.loadConfiguration(file);
    }

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

    public void addLever(Player player, Location location, boolean state) {
        String path = "levers." + location.getWorld().getName() + "." +
                location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();

        if (!isSettingUp(player)) {
            plugin.getLogger().info("Attempted to add a new lever outside of setup mode. Ignoring...");
            return;
        }

        gameData.set(path + ".state", state);
        saveConfigurations();

        plugin.getLogger().info("Added lever at " + location.toString() + " with state " + state);
    }

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

    public boolean isSettingUp(Player player) {
        return setupPlayers.contains(player);
    }

    public void startSetup(Player player) {
        setupPlayers.add(player);
        plugin.getLogger().info(player.getName() + " has entered setup mode.");
    }

    public void finishSetup(Player player) {
        setupPlayers.remove(player);
        plugin.getLogger().info(player.getName() + " has exited setup mode.");
    }

    public void checkLeverStates(Player player) {
        plugin.getLogger().info("Checking lever states...");

        if (!hasRequiredConfiguration() || !isPatternMatched() || isSettingUp(player)) {
//            plugin.getLogger().info("Required configuration not found. Aborting lever state check.");
            return;
        }

//        if (!isPatternMatched()) {
//            plugin.getLogger().info("Lever pattern not matched. Wall remains.");
//            return;
//        }
//
//        if (isSettingUp(player)) {
//            plugin.getLogger().info("Pattern matched, but player is in setup mode. Wall not removed.");
//            return;
//        }

        removeWall();
        Bukkit.broadcastMessage(ChatColor.GREEN + "The wall has been removed! Access granted.");
        plugin.getLogger().info("Lever pattern matched. Wall removed.");
    }


    private boolean hasRequiredConfiguration() {
        if (!gameData.contains("levers")) return false;
        if (gameData.getLocation("wall.topLeft") == null) return false;
        if (gameData.getLocation("wall.bottomRight") == null) return false;
        return true;
    }

    private boolean isPatternMatched() {
        ConfigurationSection leversSection = gameData.getConfigurationSection("levers");
        if (leversSection == null) {
            plugin.getLogger().warning("No 'levers' section found in game_data.yml.");
            return false;
        }

        for (String worldName : leversSection.getKeys(false)) {
            ConfigurationSection worldLevers = leversSection.getConfigurationSection(worldName);
            if (worldLevers == null) continue;

            for (String leverKey : worldLevers.getKeys(false)) {
                if (!isLeverMatched(worldName, leverKey, worldLevers)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isLeverMatched(String worldName, String leverKey, ConfigurationSection worldLevers) {
        boolean expectedState = worldLevers.getBoolean(leverKey + ".state", false);
        plugin.getLogger().info("Lever " + leverKey + " expected: " + expectedState);

        Location leverLocation = parseLeverKey(worldName, leverKey);
        if (leverLocation == null) {
            plugin.getLogger().warning("Invalid lever key: " + leverKey);
            return false;
        }

        return doesLeverMatch(leverLocation, expectedState);
    }

    private Location parseLeverKey(String worldName, String leverKey) {
        String[] coords = leverKey.split("_");
        if (coords.length != 3) {
            plugin.getLogger().warning("Invalid lever key format: " + leverKey);
            return null;
        }

        try {
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World not found: " + worldName);
                return null;
            }
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid coordinates in lever key: " + leverKey);
            return null;
        }
    }

    private boolean doesLeverMatch(Location leverLocation, boolean expectedState) {
        Block leverBlock = leverLocation.getBlock();
        if (leverBlock.getType() != Material.LEVER) {
            plugin.getLogger().warning("No lever found at " + leverLocation + ". Found " + leverBlock.getType());
            return false;
        }

        boolean currentState = !leverBlock.getBlockData().getAsString().contains("powered=true");
        plugin.getLogger().info("At " + leverLocation + " expected: " + expectedState + ", current: " + currentState);

        return currentState == expectedState;
    }

    private void removeWall() {
        plugin.getLogger().info("Removing the wall...");

        Location topLeft = gameData.getLocation("wall.topLeft");
        Location bottomRight = gameData.getLocation("wall.bottomRight");

        if (topLeft == null || bottomRight == null) {
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

    public boolean isLeverPartOfPuzzle(Location leverLocation) {
        if (!gameData.contains("levers")) return false;

        String worldName = leverLocation.getWorld().getName();
        String leverKey = leverLocation.getBlockX() + "_" + leverLocation.getBlockY() + "_" + leverLocation.getBlockZ();

        String path = "levers." + worldName + "." + leverKey;
        return gameData.contains(path);
    }
}
