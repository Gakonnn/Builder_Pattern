package edu.builder.travel;

import java.util.List;

/** Produces a structured, immutable object. */
public final class TravelPackageObjectBuilder implements TravelBuilder {
    private final TravelDraft draft = new TravelDraft();

    @Override
    public TravelPackageObjectBuilder setDestination(String destination) {
        draft.setDestination(destination);
        return this;
    }

    @Override
    public TravelPackageObjectBuilder setDays(int days) {
        draft.setDays(days);
        return this;
    }

    @Override
    public TravelPackageObjectBuilder setAccommodation(Accommodation accommodation) {
        draft.setAccommodation(accommodation);
        return this;
    }

    @Override
    public TravelPackageObjectBuilder setTransport(Transport transport) {
        draft.setTransport(transport);
        return this;
    }

    @Override
    public TravelPackageObjectBuilder setActivities(List<String> activities) {
        draft.setActivities(activities);
        return this;
    }

    public TravelPackage getResult() {
        draft.validate();
        return new TravelPackage(draft);
    }
}
