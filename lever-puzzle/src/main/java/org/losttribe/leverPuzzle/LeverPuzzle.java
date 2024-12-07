package org.losttribe.leverPuzzle;

import org.bukkit.plugin.java.JavaPlugin;

public class LeverPuzzle extends JavaPlugin {

    private LeverManager leverManager;

    @Override
    public void onEnable() {
        // Ensure the data folder exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        getLogger().info("LeverPuzzle: Initializing LeverManager.");

        // Initialize LeverManager
        leverManager = new LeverManager(this);
        getLogger().info("LeverPuzzle: LeverManager initialized.");

        // Register Commands
        if (getCommand("levers") != null) {
            getCommand("levers").setExecutor(new LeverCommand(this, leverManager));
            getLogger().info("LeverPuzzle: Command 'levers' registered.");
        } else {
            getLogger().severe("LeverPuzzle: Command 'levers' not found in plugin.yml!");
        }

        // Register Event Listeners
        getServer().getPluginManager().registerEvents(new LeverListener(this, leverManager), this);
        getLogger().info("LeverPuzzle: LeverListener registered.");

        // Configurations are loaded in LeverManager constructor

        getLogger().info("LeverPuzzle has been enabled.");
    }

    @Override
    public void onDisable() {
        if (leverManager != null) {
            leverManager.saveConfigurations();
            getLogger().info("LeverPuzzle: Configurations saved.");
        } else {
            getLogger().severe("LeverPuzzle: LeverManager was not initialized!");
        }
        getLogger().info("LeverPuzzle has been disabled.");
    }

    public LeverManager getLeverManager() {
        return leverManager;
    }
}
