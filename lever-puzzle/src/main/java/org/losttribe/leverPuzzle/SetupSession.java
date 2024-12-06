package org.losttribe.leverpuzzle;

import java.util.ArrayList;
import java.util.List;

public class SetupSession {

    private final List<LeverData> selectedLevers = new ArrayList<>();

    public void addLever(LeverData lever) {
        selectedLevers.add(lever);
    }

    public List<LeverData> getSelectedLevers() {
        return selectedLevers;
    }
}
