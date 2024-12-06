package org.losttribe.leverpuzzle;

import org.bukkit.Location;

public class LeverData {

    private final Location location;
    private final boolean state;

    public LeverData(Location location, boolean state) {
        this.location = location;
        this.state = state;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isState() {
        return state;
    }
}
