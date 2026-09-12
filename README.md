# Travel Package Builder

Java implementation of Assignment 1: the same five construction steps create an immutable `TravelPackage` object or a text itinerary. `TravelDirector` provides a Prague city break and an Almaty adventure trip.

## Run

Requires **JDK 17 or newer**. No external Java dependencies.

```sh
./run.sh
./test.sh
```

On Windows, run these scripts in Git Bash/WSL with a configured JDK, or open `src/main/java` and `src/test/java` in an IDE. Run `edu.builder.travel.Main` for the demo and `edu.builder.travel.TravelBuilderTest` for tests.

## Design

| Role | Implementation |
| --- | --- |
| Product | `TravelPackage` with private final fields and an unmodifiable copied activity list |
| Builder interface | `TravelBuilder`, five fluent setters |
| Object builder | `TravelPackageObjectBuilder.getResult()` returns `TravelPackage` |
| Text builder | `ItineraryBuilder.getResult()` returns `String` |
| Director | `TravelDirector.makeCityBreak()` and `makeAdventureTrip()` |
| Client | `Main` selects a builder and retrieves its result |
| Shared state and rules | Package-private `TravelDraft` |

The Director depends only on `TravelBuilder`; `getResult()` stays on each concrete builder because the result types differ. Both builders validate at `getResult()`. Destination must be nonblank, days must be positive, accommodation and transport must be set, and at least one nonblank activity must exist. Invalid state throws `IllegalStateException` with a clear message.

Every step returns the same builder. Setters can be called in any order. Each preset replaces all fields, so reusing a builder with another preset does not retain old activities. A manual partial update preserves fields not explicitly replaced. Builders are mutable and intended for one thread; completed products are immutable snapshots.

## Files

- `src/main/java/edu/builder/travel/` - implementation and demo.
- `src/test/java/edu/builder/travel/` - executable tests without a test framework.
- `report/Builder_Pattern_Report.pdf` - English report with UML and seven Clean Code principles illustrated by source excerpts.
- `report/sample-output.txt` and `report/test-output.txt` - captured demo and test output.
- `report/uml.puml` - editable PlantUML class diagram.
- `DEFENSE_RU.md` - Russian explanation and practice questions for the defense.
- `tools/build_report.py` - optional PDF rebuild tool; requires Python and ReportLab.

## GitHub publication

The project has incremental local Git commits. Push is deferred at the user's request; no GitHub URL has been invented. After publishing, regenerate the report with the real repository URL before submitting the PDF to Moodle:

```sh
python tools/build_report.py --github-url https://github.com/OWNER/REPOSITORY
```

Replace the example address with the actual repository URL. The report can also be regenerated without that option while publication is pending. Java compilation and execution do not require Python.
