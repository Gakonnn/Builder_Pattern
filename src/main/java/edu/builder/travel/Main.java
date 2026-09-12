package edu.builder.travel;

import java.util.List;

/** Runnable demonstration of both representations and manual fluent construction. */
public final class Main {
    private static final String CUSTOM_DESTINATION = "Kyoto";
    private static final int CUSTOM_DAYS = 4;
    private static final List<String> CUSTOM_ACTIVITIES = List.of(
            "Temple walk", "Food market visit");

    private Main() {
    }

    public static void main(String[] args) {
        TravelDirector director = new TravelDirector();
        TravelPackageObjectBuilder objectBuilder = new TravelPackageObjectBuilder();
        ItineraryBuilder itineraryBuilder = new ItineraryBuilder();

        director.makeCityBreak(objectBuilder);
        director.makeCityBreak(itineraryBuilder);
        System.out.println("=== CITY BREAK: OBJECT ===");
        System.out.println(objectBuilder.getResult());
        System.out.println("=== CITY BREAK: TEXT ===");
        System.out.print(itineraryBuilder.getResult());

        // Reuse replaces every field. Previously built products stay unchanged.
        director.makeAdventureTrip(objectBuilder);
        director.makeAdventureTrip(itineraryBuilder);
        System.out.println("\n=== ADVENTURE TRIP: OBJECT ===");
        System.out.println(objectBuilder.getResult());
        System.out.println("=== ADVENTURE TRIP: TEXT ===");
        System.out.print(itineraryBuilder.getResult());

        TravelPackage customTrip = new TravelPackageObjectBuilder()
                .setDestination(CUSTOM_DESTINATION)
                .setDays(CUSTOM_DAYS)
                .setAccommodation(Accommodation.HOTEL)
                .setTransport(Transport.FLIGHT)
                .setActivities(CUSTOM_ACTIVITIES)
                .getResult();
        System.out.println("\n=== CUSTOM FLUENT PACKAGE ===");
        System.out.println(customTrip);

        System.out.println("\n=== INVALID BUILD ===");
        try {
            new TravelPackageObjectBuilder().setDestination(" ").getResult();
        } catch (IllegalStateException exception) {
            System.out.println("Build rejected: " + exception.getMessage());
        }
    }
}
