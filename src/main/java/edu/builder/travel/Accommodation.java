package edu.builder.travel;

/** Supported accommodation choices, independent of either representation. */
public enum Accommodation {
    HOTEL("Hotel"),
    GUESTHOUSE("Guesthouse"),
    CAMPING("Camping");

    private final String displayName;

    Accommodation(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
