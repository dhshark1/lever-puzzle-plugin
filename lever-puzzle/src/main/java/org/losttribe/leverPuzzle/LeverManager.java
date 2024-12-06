package org.losttribe.leverpuzzle;

import org.losttribe.leverpuzzle.LeverPuzzle;
import org.losttribe.leverpuzzle.LeverData;
import org.losttribe.leverpuzzle.SetupSession;
import org.losttribe.leverpuzzle.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class LeverManager {

    private final LeverPuzzle plugin;
    private final Map<UUID, SetupSession> setupSessions = new HashMap<>();
    private final List<LeverData> levers = new ArrayList<>();
    private Location wallTopLeft;
    private Location wallBottomRight;

    public LeverManager(LeverPuzzle plugin) {
        this.plugin = plugin;
    }

    // Setup Management
    public void startSetup(Player player) {
        setupSessions.put(player.getUniqueId(), new SetupSession());
    }

    public void finishSetup(Player player) {
        setupSessions.remove(player.getUniqueId());
    }

    public boolean isSettingUp(Player player) {
        return setupSessions.containsKey(player.getUniqueId());
    }

    public void addLever(Player player, Location loc, boolean state) {
        LeverData lever = new LeverData(loc, state);
        setupSessions.get(player.getUniqueId()).addLever(lever);
        levers.add(lever);
        saveConfigurations();
    }

    // Wall Management
    public void setWallCorner(Player player, Location loc, boolean isTop) {
        if (isTop) {
            wallTopLeft = loc;
        } else {
            wallBottomRight = loc;
        }
        saveConfigurations();
    }

    // Configuration Management
    public void loadConfigurations() {
        loadLevers();
        loadWallCorners();
    }

    public void saveConfigurations() {
        saveLevers();
        saveWallCorners();
    }

    private void loadLevers() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection leversSection = config.getConfigurationSection("levers");

        if (leversSection != null) {
            for (String key : leversSection.getKeys(false)) {
                LeverData lever = parseLeverData(leversSection.getConfigurationSection(key));
                if (lever != null) {
                    levers.add(lever);
                }
            }
        }
    }

    private LeverData parseLeverData(ConfigurationSection section) {
        if (section == null) return null;

        String worldName = section.getString("world");
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        boolean state = section.getBoolean("state");

        if (worldName == null) return null;

        Location loc = new Location(Bukkit.getWorld(worldName), x, y, z);
        return new LeverData(loc, state);
    }

    private void loadWallCorners() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection wallSection = config.getConfigurationSection("wall");

        if (wallSection != null) {
            ConfigurationSection topSec = wallSection.getConfigurationSection("topLeft");
            ConfigurationSection bottomSec = wallSection.getConfigurationSection("bottomRight");

            if (topSec != null) {
                wallTopLeft = parseLocation(topSec);
            }

            if (bottomSec != null) {
                wallBottomRight = parseLocation(bottomSec);
            }
        }
    }

    private Location parseLocation(ConfigurationSection section) {
        if (section == null) return null;

        String worldName = section.getString("world");
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");

        if (worldName == null) return null;

        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    private void saveLevers() {
        FileConfiguration config = plugin.getConfig();
        config.set("levers", null); // Clear existing data
        ConfigurationSection leversSection = config.createSection("levers");

        for (int i = 0; i < levers.size(); i++) {
            LeverData lever = levers.get(i);
            ConfigurationSection leverSec = leversSection.createSection(String.valueOf(i));
            leverSec.set("world", lever.getLocation().getWorld().getName());
            leverSec.set("x", lever.getLocation().getX());
            leverSec.set("y", lever.getLocation().getY());
            leverSec.set("z", lever.getLocation().getZ());
            leverSec.set("state", lever.isState());
        }

        plugin.saveConfig();
    }

    private void saveWallCorners() {
        if (wallTopLeft == null || wallBottomRight == null) return;

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection wallSection = config.createSection("wall");

        ConfigurationSection topSec = wallSection.createSection("topLeft");
        LocationUtils.saveLocation(topSec, wallTopLeft);

        ConfigurationSection bottomSec = wallSection.createSection("bottomRight");
        LocationUtils.saveLocation(bottomSec, wallBottomRight);

        plugin.saveConfig();
    }

    // Lever State Checking
    public void checkLeverStates() {
        boolean isPatternCorrect = verifyLeverPattern();

        if (isPatternCorrect) {
            if (removeWall()) {
                Bukkit.broadcastMessage(ChatColor.GREEN + "The wall has been removed! Access granted.");
            } else {
                Bukkit.broadcastMessage(ChatColor.RED + "Wall coordinates are not set properly.");
            }
        } else {
            Bukkit.broadcastMessage(ChatColor.RED + "Incorrect lever combination. The wall remains.");
        }
    }

    private boolean verifyLeverPattern() {
        for (LeverData leverData : levers) {
            Block block = leverData.getLocation().getBlock();
            if (block.getType() != Material.LEVER) {
                return false;
            }
            boolean currentState = block.getBlockData().getAsString().contains("powered=true");
            if (currentState != leverData.isState()) {
                return false;
            }
        }
        return true;
    }

    private boolean removeWall() {
        if (wallTopLeft == null || wallBottomRight == null) {
            return false;
        }

        Iterable<Location> locations = LocationUtils.getLocationsBetween(wallTopLeft, wallBottomRight);

        for (Location loc : locations) {
            loc.getBlock().setType(Material.AIR);
        }

        return true;
    }
}
