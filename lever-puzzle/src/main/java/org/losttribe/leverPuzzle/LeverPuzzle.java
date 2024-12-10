package org.losttribe.leverPuzzle;

import org.bukkit.plugin.java.JavaPlugin;

public class LeverPuzzle extends JavaPlugin {

    private LeverManager leverManager;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        leverManager = new LeverManager(this);

        if (getCommand("levers") != null) {
            getCommand("levers").setExecutor(new LeverCommand(this, leverManager));
            getLogger().info("LeverPuzzle: Command 'levers' registered.");
        } else {
            getLogger().severe("LeverPuzzle: Command 'levers' not found in plugin.yml!");
        }

        getServer().getPluginManager().registerEvents(new LeverListener(this, leverManager), this);
    }

    @Override
    public void onDisable() {
        if (leverManager != null) {
            leverManager.saveConfigurations();
        } else {
            getLogger().severe("LeverPuzzle: LeverManager was not initialized!");
        }
    }

    public LeverManager getLeverManager() {
        return leverManager;
    }
}
