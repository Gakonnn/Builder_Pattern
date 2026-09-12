package edu.builder.travel;

import java.util.List;

/** An immutable snapshot of a completed travel package. */
public final class TravelPackage {
    private final String destination;
    private final int days;
    private final Accommodation accommodation;
    private final Transport transport;
    private final List<String> activities;

    // Package-private: clients construct products through a builder.
    TravelPackage(TravelDraft draft) {
        draft.validate();
        destination = draft.destination();
        days = draft.days();
        accommodation = draft.accommodation();
        transport = draft.transport();
        activities = List.copyOf(draft.activities());
    }

    public String getDestination() {
        return destination;
    }

    public int getDays() {
        return days;
    }

    public Accommodation getAccommodation() {
        return accommodation;
    }

    public Transport getTransport() {
        return transport;
    }

    public List<String> getActivities() {
        return activities;
    }

    @Override
    public String toString() {
        return "TravelPackage{destination='" + destination + "', days=" + days
                + ", accommodation=" + accommodation + ", transport=" + transport
                + ", activities=" + activities + "}";
    }
}
