package org.losttribe.leverPuzzle;

package com.yourname.leverpuzzle;

import com.yourname.leverpuzzle.commands.LeverCommand;
import com.yourname.leverpuzzle.listeners.LeverListener;
import com.yourname.leverpuzzle.managers.LeverManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LeverPuzzle extends JavaPlugin {

    private LeverManager leverManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize managers
        leverManager = new LeverManager(this);

        // Register commands
        getCommand("levers").setExecutor(new LeverCommand(this, leverManager));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new LeverListener(this, leverManager), this);

        // Load existing configurations
        leverManager.loadConfigurations();

        getLogger().info("LeverPuzzle has been enabled.");
    }

    @Override
    public void onDisable() {
        // Save configurations on disable
        leverManager.saveConfigurations();
        getLogger().info("LeverPuzzle has been disabled.");
    }

    public LeverManager getLeverManager() {
        return leverManager;
    }
}
