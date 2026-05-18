# Cardio Data Simulator

The Cardio Data Simulator is a Java-based application that simulates real-time
cardiovascular data for multiple patients. The data stream covers ECG, blood
pressure, blood saturation, and other cardiovascular signals, and is consumed
by a downstream monitoring/alert pipeline.

This repository is a fork of [`mpietrasik/signal_project`](https://github.com/mpietrasik/signal_project)
extended week-by-week for the KEN1520 Software Engineering project.

## Features

- Simulates real-time ECG, blood pressure, blood saturation, and blood-levels data.
- Pluggable output strategies:
  - `console` for direct observation
  - `file:<dir>` for persistence
  - `tcp:<port>` and `websocket:<port>` for networked streaming
- Configurable patient count and randomized patient ID assignment.
- Downstream alert engine with five alert types (BP critical / BP trend,
  saturation low / saturation rapid drop, hypotensive hypoxemia, ECG abnormal
  peak, nurse-call manual alerts).
- Real-time ingestion via a `WebSocketDataReader` that continuously feeds the
  in-memory `DataStorage`.

## Project structure

```
src/main/java/
├── com/cardio_generator/        — the simulator (data generators + outputs)
├── com/data_management/         — storage and the DataReader implementations
└── com/alerts/                  — alert engine + design-pattern packages
    ├── factories/               — Factory Method pattern
    ├── strategies/              — Strategy pattern
    └── decorators/              — Decorator pattern
src/test/java/                   — JUnit 5 tests, ~50 cases total
uml_models/                      — UML class diagrams (see below)
```

## Getting Started

### Prerequisites

- Java JDK 11 or newer.
- Maven 3.6+ for dependency management and building.

### Build

```sh
mvn clean package
```

Produces an executable JAR at `target/cardio_generator-1.0-SNAPSHOT.jar`.
Tests run automatically during the `test` phase before packaging.

### Run the simulator

```sh
# Console output, 50 patients (defaults)
java -jar target/cardio_generator-1.0-SNAPSHOT.jar

# 100 patients, write to ./output as files
java -jar target/cardio_generator-1.0-SNAPSHOT.jar --patient-count 100 --output file:./output

# Broadcast over a WebSocket on port 8080
java -jar target/cardio_generator-1.0-SNAPSHOT.jar --output websocket:8080
```

### Run the data-management / alert side

After the simulator has written some files, you can load and evaluate them:

```sh
mvn exec:java -Dexec.mainClass="com.data_management.DataStorage" -Dexec.args="./output"
```

### Run tests + coverage report

```sh
mvn test                  # just the tests
mvn package               # tests + JaCoCo coverage report
```

The JaCoCo HTML report is at `target/site/jacoco/index.html`.

## UML Models

The four required UML class diagrams for the CHMS subsystems live in
[`uml_models/`](./uml_models). Each diagram is provided as both an editable
PlantUML source (`.puml`) and a rendered PNG:

| # | Subsystem                        | PlantUML                                          | PNG                                                  |
|---|----------------------------------|---------------------------------------------------|------------------------------------------------------|
| 1 | Alert Generation System          | [1_alert_generation_system.puml](uml_models/1_alert_generation_system.puml) | [AlertGenerationSystem.png](uml_models/AlertGenerationSystem.png) |
| 2 | Data Storage System              | [2_data_storage_system.puml](uml_models/2_data_storage_system.puml) | [DataStorageSystem.png](uml_models/DataStorageSystem.png) |
| 3 | Patient Identification System    | [3_patient_identification_system.puml](uml_models/3_patient_identification_system.puml) | [PatientIdentificationSystem.png](uml_models/PatientIdentificationSystem.png) |
| 4 | Data Access Layer                | [4_data_access_layer.puml](uml_models/4_data_access_layer.puml) | [DataAccessLayer.png](uml_models/DataAccessLayer.png) |

The rationale paragraph for each diagram is in
[`uml_models/RATIONALE.md`](./uml_models/RATIONALE.md).

## Design patterns implemented (Week 4)

| Pattern        | Where                                                                                       | Why                                                                                                       |
|----------------|---------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| Factory Method | `com.alerts.factories.AlertFactory` + 3 concrete factories                                  | New alert families can be added without touching existing factories (Open/Closed Principle).              |
| Strategy       | `com.alerts.strategies.AlertStrategy` + 3 concrete strategies                               | Each monitoring rule (BP, SpO₂, HR) is swappable, isolated, and unit-testable.                            |
| Decorator      | `com.alerts.decorators.AlertDecorator` + `PriorityAlertDecorator`, `RepeatedAlertDecorator` | Adds priority / repeat semantics to an alert without changing the `Alert` class — and decorators stack.    |
| Singleton      | `DataStorage`, `HealthDataSimulator`                                                        | Exactly one repository / orchestrator per JVM. Public constructor on `DataStorage` is retained for tests. |

## Supported output options

- `console`: prints data to stdout.
- `file:<dir>`: appends one line per measurement to `<dir>/<label>.txt`.
- `websocket:<port>`: broadcasts CSV lines to all connected clients.
- `tcp:<port>`: streams CSV lines to one connected client.

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.

## Project Members

- Student ID: 6439058
