package edu.builder.travel;

import java.util.List;

/** Produces a readable text itinerary using the same construction steps. */
public final class ItineraryBuilder implements TravelBuilder {
    private final TravelDraft draft = new TravelDraft();

    @Override
    public ItineraryBuilder setDestination(String destination) {
        draft.setDestination(destination);
        return this;
    }

    @Override
    public ItineraryBuilder setDays(int days) {
        draft.setDays(days);
        return this;
    }

    @Override
    public ItineraryBuilder setAccommodation(Accommodation accommodation) {
        draft.setAccommodation(accommodation);
        return this;
    }

    @Override
    public ItineraryBuilder setTransport(Transport transport) {
        draft.setTransport(transport);
        return this;
    }

    @Override
    public ItineraryBuilder setActivities(List<String> activities) {
        draft.setActivities(activities);
        return this;
    }

    public String getResult() {
        draft.validate();
        StringBuilder text = new StringBuilder("TRAVEL ITINERARY\n");
        text.append("Destination: ").append(draft.destination()).append('\n');
        text.append("Duration: ").append(draft.days()).append(" days\n");
        text.append("Accommodation: ").append(draft.accommodation().getDisplayName()).append('\n');
        text.append("Transport: ").append(draft.transport().getDisplayName()).append('\n');
        text.append("Activities:\n");
        for (String activity : draft.activities()) {
            text.append("  - ").append(activity).append('\n');
        }
        return text.toString();
    }
}
