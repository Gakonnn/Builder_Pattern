package edu.builder.travel;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Dependency-free behavioral tests: failures throw AssertionError and exit nonzero. */
public final class TravelBuilderTest {
    private static final List<Supplier<BuilderHarness>> BUILDERS = List.of(
            () -> {
                TravelPackageObjectBuilder builder = new TravelPackageObjectBuilder();
                return new BuilderHarness(builder, builder::getResult);
            },
            () -> {
                ItineraryBuilder builder = new ItineraryBuilder();
                return new BuilderHarness(builder, builder::getResult);
            });
    private static int passed;

    private TravelBuilderTest() {
    }

    public static void main(String[] args) {
        run("All fluent steps return the same builder", TravelBuilderTest::fluentSteps);
        run("Both builders reject missing and invalid fields", TravelBuilderTest::invalidStates);
        run("City break has matching object and text values", TravelBuilderTest::cityBreak);
        run("Adventure trip has matching object and text values", TravelBuilderTest::adventureTrip);
        run("Director uses the same five interface steps", TravelBuilderTest::directorSteps);
        run("Mutable input lists are copied by both builders", TravelBuilderTest::inputCopy);
        run("Product activities cannot be modified", TravelBuilderTest::immutableActivities);
        run("Builder reuse preserves old products and replaces activities", TravelBuilderTest::reuse);
        run("Destination and activity whitespace is normalized", TravelBuilderTest::normalization);
        run("Every getResult validates current state", TravelBuilderTest::revalidation);
        run("Product exposes no public construction or mutable fields", TravelBuilderTest::productStructure);
        run("Concrete fluent chains expose the right result types", TravelBuilderTest::typedChains);
        System.out.println("All " + passed + " tests passed.");
    }

    private static void fluentSteps() {
        for (Supplier<BuilderHarness> factory : BUILDERS) {
            TravelBuilder builder = factory.get().builder();
            same(builder, builder.setDestination("Prague"));
            same(builder, builder.setDays(3));
            same(builder, builder.setAccommodation(Accommodation.HOTEL));
            same(builder, builder.setTransport(Transport.TRAIN));
            same(builder, builder.setActivities(List.of("Walking tour")));
        }
    }

    private static void invalidStates() {
        List<InvalidCase> cases = List.of(
                new InvalidCase(b -> b.setDestination(null), "Destination must not be blank."),
                new InvalidCase(b -> b.setDestination(" \t\n"), "Destination must not be blank."),
                new InvalidCase(b -> b.setDays(0), "Duration must be at least 1 day."),
                new InvalidCase(b -> b.setDays(-2), "Duration must be at least 1 day."),
                new InvalidCase(b -> b.setAccommodation(null), "Accommodation is required."),
                new InvalidCase(b -> b.setTransport(null), "Transport is required."),
                new InvalidCase(b -> b.setActivities(null), "At least one activity is required."),
                new InvalidCase(b -> b.setActivities(List.of()), "At least one activity is required."),
                new InvalidCase(b -> b.setActivities(Arrays.asList("Walk", null)),
                        "Activities must not contain null or blank entries."),
                new InvalidCase(b -> b.setActivities(List.of("Walk", "  ")),
                        "Activities must not contain null or blank entries."));
        for (Supplier<BuilderHarness> factory : BUILDERS) {
            BuilderHarness empty = factory.get();
            expectIllegalState(empty.result(), "Destination must not be blank.");
            empty.builder().setDestination("Prague");
            expectIllegalState(empty.result(), "Duration must be at least 1 day.");
            empty.builder().setDays(3);
            expectIllegalState(empty.result(), "Accommodation is required.");
            empty.builder().setAccommodation(Accommodation.HOTEL);
            expectIllegalState(empty.result(), "Transport is required.");
            empty.builder().setTransport(Transport.TRAIN);
            expectIllegalState(empty.result(), "At least one activity is required.");
            for (InvalidCase invalid : cases) {
                BuilderHarness harness = factory.get();
                new TravelDirector().makeCityBreak(harness.builder());
                invalid.mutation().accept(harness.builder());
                expectIllegalState(harness.result(), invalid.message());
            }
        }
    }

    private static void cityBreak() {
        TravelPackageObjectBuilder object = new TravelPackageObjectBuilder();
        ItineraryBuilder text = new ItineraryBuilder();
        TravelDirector director = new TravelDirector();
        same(object, director.makeCityBreak(object));
        same(text, director.makeCityBreak(text));
        TravelPackage trip = object.getResult();
        equal("Prague", trip.getDestination());
        equal(3, trip.getDays());
        equal(Accommodation.HOTEL, trip.getAccommodation());
        equal(Transport.TRAIN, trip.getTransport());
        equal(List.of("Old Town walking tour", "Prague Castle visit"), trip.getActivities());
        equal("TRAVEL ITINERARY\nDestination: Prague\nDuration: 3 days\n"
                + "Accommodation: Hotel\nTransport: Train\nActivities:\n"
                + "  - Old Town walking tour\n  - Prague Castle visit\n", text.getResult());
    }

    private static void adventureTrip() {
        TravelPackageObjectBuilder object = new TravelPackageObjectBuilder();
        ItineraryBuilder text = new ItineraryBuilder();
        TravelDirector director = new TravelDirector();
        director.makeAdventureTrip(object);
        director.makeAdventureTrip(text);
        TravelPackage trip = object.getResult();
        equal("Almaty", trip.getDestination());
        equal(5, trip.getDays());
        equal(Accommodation.GUESTHOUSE, trip.getAccommodation());
        equal(Transport.MINIBUS, trip.getTransport());
        equal(List.of("Mountain hike", "Lake visit"), trip.getActivities());
        equal("TRAVEL ITINERARY\nDestination: Almaty\nDuration: 5 days\n"
                + "Accommodation: Guesthouse\nTransport: Minibus\nActivities:\n"
                + "  - Mountain hike\n  - Lake visit\n", text.getResult());
    }

    private static void directorSteps() {
        StepRecorder city = new StepRecorder();
        StepRecorder adventure = new StepRecorder();
        TravelDirector director = new TravelDirector();
        director.makeCityBreak(city);
        director.makeAdventureTrip(adventure);
        List<String> expected = List.of("destination", "days", "accommodation", "transport", "activities");
        equal(expected, city.steps);
        equal(expected, adventure.steps);
    }

    private static void inputCopy() {
        for (Supplier<BuilderHarness> factory : BUILDERS) {
            BuilderHarness harness = factory.get();
            new TravelDirector().makeCityBreak(harness.builder());
            List<String> mutable = new ArrayList<>(List.of("Museum visit"));
            harness.builder().setActivities(mutable);
            mutable.set(0, "Unexpected replacement");
            mutable.add(null);
            Object result = harness.result().get();
            if (result instanceof TravelPackage trip) {
                equal(List.of("Museum visit"), trip.getActivities());
            } else {
                check(result.toString().endsWith("Activities:\n  - Museum visit\n"), "Text used mutated input");
            }
        }
    }

    private static void immutableActivities() {
        TravelPackageObjectBuilder builder = new TravelPackageObjectBuilder();
        new TravelDirector().makeCityBreak(builder);
        List<String> activities = builder.getResult().getActivities();
        expectUnsupported(() -> activities.add("Extra activity"));
        expectUnsupported(() -> activities.set(0, "Replacement"));
        expectUnsupported(() -> activities.remove(0));
    }

    private static void reuse() {
        TravelDirector director = new TravelDirector();
        TravelPackageObjectBuilder object = new TravelPackageObjectBuilder();
        ItineraryBuilder text = new ItineraryBuilder();
        director.makeCityBreak(object);
        director.makeCityBreak(text);
        TravelPackage oldTrip = object.getResult();
        String oldText = text.getResult();
        director.makeAdventureTrip(object);
        director.makeAdventureTrip(text);
        TravelPackage newTrip = object.getResult();
        equal("Prague", oldTrip.getDestination());
        equal(3, oldTrip.getDays());
        equal(Accommodation.HOTEL, oldTrip.getAccommodation());
        equal(Transport.TRAIN, oldTrip.getTransport());
        equal(List.of("Old Town walking tour", "Prague Castle visit"), oldTrip.getActivities());
        equal("Almaty", newTrip.getDestination());
        equal(List.of("Mountain hike", "Lake visit"), newTrip.getActivities());
        check(oldText.contains("Destination: Prague"), "Old text changed");
        check(!text.getResult().contains("Prague"), "Activities accumulated between presets");
        object.setActivities(List.of("New activity"));
        equal(List.of("Mountain hike", "Lake visit"), newTrip.getActivities());
        equal(List.of("New activity"), object.getResult().getActivities());
    }

    private static void normalization() {
        for (Supplier<BuilderHarness> factory : BUILDERS) {
            BuilderHarness harness = factory.get();
            new TravelDirector().makeCityBreak(harness.builder());
            harness.builder().setDestination("  Prague\t").setActivities(List.of("  Museum visit\n"));
            Object result = harness.result().get();
            if (result instanceof TravelPackage trip) {
                equal("Prague", trip.getDestination());
                equal(List.of("Museum visit"), trip.getActivities());
            } else {
                check(result.toString().contains("Destination: Prague\n"), "Destination was not normalized");
                check(result.toString().endsWith("  - Museum visit\n"), "Activity was not normalized");
            }
        }
    }

    private static void revalidation() {
        for (Supplier<BuilderHarness> factory : BUILDERS) {
            BuilderHarness harness = factory.get();
            new TravelDirector().makeCityBreak(harness.builder());
            harness.result().get();
            harness.builder().setTransport(null);
            expectIllegalState(harness.result(), "Transport is required.");
            harness.builder().setTransport(Transport.TRAIN);
            harness.result().get();
        }
    }

    private static void productStructure() {
        check(Modifier.isFinal(TravelPackage.class.getModifiers()), "Product must be final");
        equal(0, TravelPackage.class.getConstructors().length);
        for (var field : TravelPackage.class.getDeclaredFields()) {
            check(Modifier.isPrivate(field.getModifiers()), "Product field must be private: " + field.getName());
            check(Modifier.isFinal(field.getModifiers()), "Product field must be final: " + field.getName());
        }
        for (var method : TravelPackage.class.getMethods()) {
            check(!method.getName().startsWith("set"), "Product must expose no setters");
        }
    }

    private static void typedChains() {
        TravelPackage object = new TravelPackageObjectBuilder().setDestination("Kyoto").setDays(4)
                .setAccommodation(Accommodation.HOTEL).setTransport(Transport.FLIGHT)
                .setActivities(List.of("Temple walk")).getResult();
        String text = new ItineraryBuilder().setDestination("Kyoto").setDays(4)
                .setAccommodation(Accommodation.HOTEL).setTransport(Transport.FLIGHT)
                .setActivities(List.of("Temple walk")).getResult();
        equal("Kyoto", object.getDestination());
        check(text.contains("Destination: Kyoto"), "Concrete fluent text chain failed");
    }

    private static void run(String name, Runnable test) {
        test.run();
        passed++;
        System.out.println("PASS: " + name);
    }

    private static void equal(Object expected, Object actual) {
        check(Objects.equals(expected, actual), "Expected <" + expected + "> but was <" + actual + ">");
    }

    private static void same(Object expected, Object actual) {
        check(expected == actual, "Expected the same builder instance");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expectIllegalState(Supplier<Object> operation, String message) {
        try {
            operation.get();
        } catch (IllegalStateException exception) {
            equal(message, exception.getMessage());
            return;
        }
        throw new AssertionError("Expected IllegalStateException: " + message);
    }

    private static void expectUnsupported(Runnable operation) {
        try {
            operation.run();
        } catch (UnsupportedOperationException expected) {
            return;
        }
        throw new AssertionError("Expected an immutable activities list");
    }

    private record BuilderHarness(TravelBuilder builder, Supplier<Object> result) {
    }

    private record InvalidCase(Consumer<TravelBuilder> mutation, String message) {
    }

    private static final class StepRecorder implements TravelBuilder {
        private final List<String> steps = new ArrayList<>();

        public TravelBuilder setDestination(String value) { steps.add("destination"); return this; }
        public TravelBuilder setDays(int value) { steps.add("days"); return this; }
        public TravelBuilder setAccommodation(Accommodation value) { steps.add("accommodation"); return this; }
        public TravelBuilder setTransport(Transport value) { steps.add("transport"); return this; }
        public TravelBuilder setActivities(List<String> value) { steps.add("activities"); return this; }
    }
}
