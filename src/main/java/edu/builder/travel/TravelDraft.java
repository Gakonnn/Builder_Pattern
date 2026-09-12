package edu.builder.travel;

import java.util.ArrayList;
import java.util.List;

/** Mutable construction state shared by the builders, never exposed to clients. */
final class TravelDraft {
    private static final int MINIMUM_DAYS = 1;

    private String destination;
    private int days;
    private Accommodation accommodation;
    private Transport transport;
    private List<String> activities;

    void setDestination(String destination) {
        this.destination = normalize(destination);
    }

    void setDays(int days) {
        this.days = days;
    }

    void setAccommodation(Accommodation accommodation) {
        this.accommodation = accommodation;
    }

    void setTransport(Transport transport) {
        this.transport = transport;
    }

    void setActivities(List<String> activities) {
        if (activities == null) {
            this.activities = null;
            return;
        }
        // Take a defensive copy immediately; defer invalid-state errors to getResult().
        this.activities = new ArrayList<>(activities.size());
        for (String activity : activities) {
            this.activities.add(normalize(activity));
        }
    }

    void validate() {
        if (destination == null || destination.isBlank()) {
            throw new IllegalStateException("Destination must not be blank.");
        }
        if (days < MINIMUM_DAYS) {
            throw new IllegalStateException("Duration must be at least " + MINIMUM_DAYS + " day.");
        }
        if (accommodation == null) {
            throw new IllegalStateException("Accommodation is required.");
        }
        if (transport == null) {
            throw new IllegalStateException("Transport is required.");
        }
        if (activities == null || activities.isEmpty()) {
            throw new IllegalStateException("At least one activity is required.");
        }
        for (String activity : activities) {
            if (activity == null || activity.isBlank()) {
                throw new IllegalStateException("Activities must not contain null or blank entries.");
            }
        }
    }

    String destination() {
        return destination;
    }

    int days() {
        return days;
    }

    Accommodation accommodation() {
        return accommodation;
    }

    Transport transport() {
        return transport;
    }

    List<String> activities() {
        return List.copyOf(activities);
    }

    private static String normalize(String text) {
        return text == null ? null : text.strip();
    }
}
