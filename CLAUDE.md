# Bonita Salesforce Connector — CLAUDE.md

## Project Overview

**Artifact:** `org.bonitasoft.connectors:bonita-connector-salesforce`
**Current version:** 2.0.0-SNAPSHOT
**Bonita runtime:** 12.0+ (Jakarta EE, Java 17); Bonita 7.13 - 11.x use the 1.x versions
**License:** GPL v2.0
**Description:** Salesforce Connectors for Bonita — a set of connectors that allow Bonita BPM processes to interact with Salesforce via the Salesforce Partner API (SOAP).

The connector suite provides five operations:
| Connector | Class | Purpose |
|---|---|---|
| salesforce-createsobject | `CreateSObjectConnector` | Create a Salesforce SObject record |
| salesforce-deletesobjects | `DeleteSObjectsConnector` | Delete one or more SObject records by ID |
| salesforce-querysobjects | `QuerySObjectsConnector` | Run a SOQL query and return matching records |
| salesforce-retrievesobjects | `RetrieveSObjectsConnector` | Retrieve specific SObject records by ID |
| salesforce-updatesobject | `UpdateSObjectConnector` | Update fields on an existing SObject record |

All connectors extend the abstract `SalesforceConnector` base class which handles authentication, connection lifecycle, and input validation.

## Build Commands

The project uses Maven Wrapper (`mvnw`). Java 17 is the compile target and the CI JDK.

```bash
# Full build (compile + test + package + JaCoCo coverage)
./mvnw verify

# Build skipping tests
./mvnw verify -DskipTests

# Run tests only
./mvnw test

# Clean build
./mvnw clean verify

# Build and run SonarCloud analysis (requires SONAR_TOKEN env var)
./mvnw clean verify sonar:sonar

# Check license headers on all Java files
./mvnw validate

# Package with GPG signing for release to Maven Central
./mvnw clean verify -P deploy
```

The default Maven goal is `verify`. Build output (ZIP assemblies + JARs) lands in `target/`.

## Architecture

### Source layout

```
src/
  main/
    java/org/bonitasoft/connectors/salesforce/partner/
      SalesforceConnector.java          # Abstract base — auth, lifecycle, validation
      CreateSObjectConnector.java       # Create a single SObject
      DeleteSObjectsConnector.java      # Delete one or more SObjects
      QuerySObjectsConnector.java       # SOQL query
      RetrieveSObjectsConnector.java    # Retrieve SObjects by ID list
      UpdateSObjectConnector.java       # Update a single SObject
    resources/                          # i18n .properties + icon images
    resources-filtered/                 # .def and .impl descriptor files (Maven-filtered)
  assembly/                             # Maven Assembly descriptors — one ZIP per connector
  script/                               # Groovy script (dependencies-as-var.groovy) run at generate-resources
  license/header.txt                    # GPL-v2 license header for Java files
  test/
    java/org/bonitasoft/connectors/salesforce/partner/
      SalesforceConnectorTest.java      # Base connector validation & lifecycle tests
      CreateSObjectConnectorTest.java
      DeleteSObjectsConnectorTest.java
      QuerySObjectsConnectorTest.java
      RetrieveSObjectsConnectorTest.java
      UpdateSObjectConnectorTest.java
      ConnectorConfiguration.java       # Shared test helper — default input params map
```

### Key design decisions

- **`SalesforceConnector`** extends Bonita's `AbstractConnector`. It implements the connect/disconnect/executeBusinessLogic lifecycle and provides shared validation (username, password, securityToken, endpoint API version, timeout/port range checks). Each subclass only needs to implement `executeFunction(PartnerConnection)` and `validateExtraValues()`.
- **Auto-reconnect:** If `executeFunction` throws a `ConnectionException`, the base class logs out, reconnects, and retries once before throwing `ConnectorException`.
- **Salesforce API version:** The Partner API endpoint must end with `/24.0`. The connector enforces this at validation time.
- **Assembly packaging:** Each connector is packaged as a separate ZIP via `maven-assembly-plugin`, using descriptors in `src/assembly/`. The ZIP contains the JAR plus all runtime dependencies (excluding `bonita-common` and `commons-logging`, which are `provided` by the Bonita runtime).
- **Connector descriptors:** `.def` (definition) and `.impl` (implementation) files in `src/main/resources-filtered/` are Maven-filtered to inject version and class name properties from `pom.xml`.

### Key dependencies

| Dependency | Version | Scope |
|---|---|---|
| `org.bonitasoft.runtime:bonita-runtime-bom` | 12.0-SNAPSHOT | import (dependencyManagement) |
| `org.bonitasoft.engine:bonita-common` | from the BOM | provided |
| `com.force.api:force-wsc` | 59.0.0 | compile |
| `com.force.api:force-partner-api` | 59.0.0 | compile |
| `commons-logging:commons-logging` | 1.3.5 | provided (logging facade of the runtime, excluded from force-wsc) |
| JUnit Jupiter | 5.10.1 | test |
| Mockito | 5.8.0 | test |
| AssertJ | 3.24.2 | test |

## Testing

Tests use JUnit 5 (Jupiter) + Mockito + AssertJ. All test classes follow the `*Test.java` naming convention (maven-surefire also picks up `*IT.java`).

- **Unit tests:** Each connector class has a corresponding `*ConnectorTest.java`. Tests mock the `PartnerConnection` using Mockito and call `validateInputParameters()` / `executeBusinessLogic()` directly.
- **`ConnectorConfiguration`** provides a `defaultConfiguration()` helper that returns a valid input-parameter map so individual tests only override what they need.
- **Coverage:** JaCoCo is configured to run on every build (`verify` phase). Reports are generated under `target/site/jacoco/`.
- **No integration tests against live Salesforce:** Tests are pure unit tests — no real Salesforce org credentials are required.

To run a single test class:
```bash
./mvnw test -Dtest=CreateSObjectConnectorTest
```

## Commit Format

Follow the format defined in `CONTRIBUTING.md` (used by the automated changelog generator):

```
type(category): description [flags]

<optional body>
```

Valid types: `breaking`, `build`, `ci`, `chore`, `docs`, `feat`, `fix`, `other`, `perf`, `refactor`, `revert`, `style`, `test`

Optional flag: `[breaking]` — marks the commit as a breaking change regardless of type.

Examples:
```
feat(create): support upsert operation
fix(validation): handle null sObjectType gracefully
chore(deps): bump force-partner-api to 60.0.0
ci(workflows): add Claude Code review workflow
```

## Release Process

1. Releases are triggered via the `release.yml` GitHub Actions workflow.
2. The project publishes to **Maven Central** via the `central-publishing-maven-plugin` (Sonatype Central Portal).
3. Artifacts are GPG-signed using the `deploy` Maven profile.
4. Each connector is also packaged as a ZIP assembly (for direct Bonita Studio import).
5. The SCM is configured at `git@github.com:bonitasoft/bonita-connector-salesforce.git`.

To prepare a release locally (dry-run):
```bash
./mvnw clean verify -P deploy -DskipTests
```

SonarCloud project: `bonitasoft_bonita-connector-salesforce` (organization: `bonitasoft`, host: `https://sonarcloud.io`).
