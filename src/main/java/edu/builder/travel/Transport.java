package edu.builder.travel;

/** Supported transport choices. */
public enum Transport {
    TRAIN("Train"),
    MINIBUS("Minibus"),
    FLIGHT("Flight");

    private final String displayName;

    Transport(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
