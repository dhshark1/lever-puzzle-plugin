package org.losttribe.leverpuzzle;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class LocationUtils {

    /**
     * Saves a Location to a ConfigurationSection.
     *
     * @param section The configuration section to save to.
     * @param loc     The location to save.
     */
    public static void saveLocation(ConfigurationSection section, Location loc) {
        section.set("world", loc.getWorld().getName());
        section.set("x", loc.getX());
        section.set("y", loc.getY());
        section.set("z", loc.getZ());
    }

    /**
     * Retrieves all Locations between two corners, inclusive.
     *
     * @param loc1 One corner.
     * @param loc2 The opposite corner.
     * @return A list of all Locations within the defined cuboid.
     */
    public static List<Location> getLocationsBetween(Location loc1, Location loc2) {
        List<Location> locations = new ArrayList<>();

        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());

        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());

        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    locations.add(new Location(loc1.getWorld(), x, y, z));
                }
            }
        }

        return locations;
    }
}
