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
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfigurations() {
        File file = new File(plugin.getDataFolder(), "game_data.yml");
        try {
            gameData.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addLever(Player player, Location location, boolean state) {
        String path = "levers." + location.getWorld().getName() + "." +
                location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();

        if (!isSettingUp(player)) {
            return;
        }

        gameData.set(path + ".state", state);
        saveConfigurations();

    }

    public void setWallCorner(Player player, Location loc, boolean isTop) {

        if (isTop) {
            gameData.set("wall.topLeft", loc);
        } else {
            gameData.set("wall.bottomRight", loc);
        }
        saveConfigurations();
    }

    public boolean isSettingUp(Player player) {
        return setupPlayers.contains(player);
    }

    public void startSetup(Player player) {
        setupPlayers.add(player);
    }

    public void finishSetup(Player player) {
        setupPlayers.remove(player);
    }

    public void checkLeverStates(Player player) {

        if (!hasRequiredConfiguration() || !isPatternMatched() || isSettingUp(player)) {
            return;
        }

        removeWall();
        Bukkit.broadcastMessage(ChatColor.GREEN + "The wall has been removed! Access granted.");
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

        Location leverLocation = parseLeverKey(worldName, leverKey);
        if (leverLocation == null) {
            return false;
        }

        return doesLeverMatch(leverLocation, expectedState);
    }

    private Location parseLeverKey(String worldName, String leverKey) {
        String[] coords = leverKey.split("_");
        if (coords.length != 3) {
            return null;
        }

        try {
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return null;
            }
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean doesLeverMatch(Location leverLocation, boolean expectedState) {
        Block leverBlock = leverLocation.getBlock();
        if (leverBlock.getType() != Material.LEVER) {
            return false;
        }

        boolean currentState = !leverBlock.getBlockData().getAsString().contains("powered=true");

        return currentState == expectedState;
    }

    private void removeWall() {

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

    }

    public boolean isLeverPartOfPuzzle(Location leverLocation) {
        if (!gameData.contains("levers")) return false;

        String worldName = leverLocation.getWorld().getName();
        String leverKey = leverLocation.getBlockX() + "_" + leverLocation.getBlockY() + "_" + leverLocation.getBlockZ();

        String path = "levers." + worldName + "." + leverKey;
        return gameData.contains(path);
    }
}
