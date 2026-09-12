package edu.builder.travel;

import java.util.List;
import java.util.Objects;

/** Reusable recipes that depend only on the common Builder interface. */
public final class TravelDirector {
    private static final String CITY_DESTINATION = "Prague";
    private static final int CITY_DAYS = 3;
    private static final Accommodation CITY_ACCOMMODATION = Accommodation.HOTEL;
    private static final Transport CITY_TRANSPORT = Transport.TRAIN;
    private static final List<String> CITY_ACTIVITIES = List.of(
            "Old Town walking tour", "Prague Castle visit");

    private static final String ADVENTURE_DESTINATION = "Almaty";
    private static final int ADVENTURE_DAYS = 5;
    private static final Accommodation ADVENTURE_ACCOMMODATION = Accommodation.GUESTHOUSE;
    private static final Transport ADVENTURE_TRANSPORT = Transport.MINIBUS;
    private static final List<String> ADVENTURE_ACTIVITIES = List.of(
            "Mountain hike", "Lake visit");

    public TravelBuilder makeCityBreak(TravelBuilder builder) {
        Objects.requireNonNull(builder, "Builder is required.");
        return builder.setDestination(CITY_DESTINATION)
                .setDays(CITY_DAYS)
                .setAccommodation(CITY_ACCOMMODATION)
                .setTransport(CITY_TRANSPORT)
                .setActivities(CITY_ACTIVITIES);
    }

    public TravelBuilder makeAdventureTrip(TravelBuilder builder) {
        Objects.requireNonNull(builder, "Builder is required.");
        return builder.setDestination(ADVENTURE_DESTINATION)
                .setDays(ADVENTURE_DAYS)
                .setAccommodation(ADVENTURE_ACCOMMODATION)
                .setTransport(ADVENTURE_TRANSPORT)
                .setActivities(ADVENTURE_ACTIVITIES);
    }
}
