package edu.builder.travel;

import java.util.List;

/** Common fluent construction steps; results have builder-specific types. */
public interface TravelBuilder {
    TravelBuilder setDestination(String destination);

    TravelBuilder setDays(int days);

    TravelBuilder setAccommodation(Accommodation accommodation);

    TravelBuilder setTransport(Transport transport);

    TravelBuilder setActivities(List<String> activities);
}
