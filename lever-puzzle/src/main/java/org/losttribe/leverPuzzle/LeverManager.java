package org.losttribe.leverPuzzle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
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

        boolean leverExists = gameData.contains(path);

        if (!leverExists && !isSettingUp(player)) {
            plugin.getLogger().info("Attempted to add a new lever outside of setup mode. Ignoring...");
            return;
        }

        gameData.set(path + ".state", state);
        saveConfigurations();

        plugin.getLogger().info((leverExists ? "Updated" : "Added")
                + " lever at " + location.toString() + " with state " + state);
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

        if (!gameData.contains("levers") || gameData.getLocation("wall.bottomRight") == null || gameData.getLocation("wall.topLeft") == null) {
            return;
        }

        boolean allLeversPowered = true;

        for (String world : gameData.getConfigurationSection("levers").getKeys(false)) {
            for (String leverKey : gameData.getConfigurationSection("levers." + world).getKeys(false)) {
                boolean state = gameData.getBoolean("levers." + world + "." + leverKey + ".state");
                plugin.getLogger().info(leverKey + " " + state);

                if (!state) {
                    allLeversPowered = false;
                    break;
                }
            }
            if (!allLeversPowered) break;
        }

        if (allLeversPowered && !isSettingUp(player)) {
            removeWall();
            Bukkit.broadcastMessage(ChatColor.GREEN + "The wall has been removed! Access granted.");
            plugin.getLogger().info("All levers are powered. Wall removed.");
        } else {
            plugin.getLogger().info("Lever pattern not matched. Wall remains.");
        }
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
}
