# DSpace 9.2 Workspace Guidelines

These guidelines help AI agents be productive when working on DSpace code and enhancements.

## Quick Build Reference

```bash
# Fast development build (no tests, no checks)
mvn clean install -DskipTests

# Build specific module
mvn -pl dspace-api clean install

# Run tests (skipped by default)
mvn install -DskipUnitTests=false -DskipIntegrationTests=false

# Fast rebuild with quality checks
mvn clean install -DskipTests -DskipIntegrationTests=true
```

**Critical**: First build takes 10+ minutes. Java 17+ and Maven 3.8+ required.

## Architecture

DSpace is a multi-module Maven project where REST endpoints delegate to service layers:

| Module | Purpose | Location |
|--------|---------|----------|
| **dspace-api** | Business logic, services, database models | `/dspace-api/src/main/java/org/dspace/` |
| **dspace-server-webapp** | REST API controllers & Spring configuration | `/dspace-server-webapp/src/.../dspace/app/rest/` |
| **dspace-services** | Service locator, plugin system | `/dspace-services/` |
| **dspace-oai, dspace-rdf, dspace-iiif, dspace-sword, dspace-saml2** | Protocol/auth modules | Specialize per module name |

### REST API Pattern

- Controllers in: `dspace-server-webapp/src/main/java/org/dspace/app/rest/`
- Service layer in: `dspace-api/src/main/java/org/dspace/{resource}/service/`
- Endpoint format: `GET|POST /api/{resource}/{action}`
- Authentication: Spring Security with Bearer tokens

## Code Conventions

- **Indentation**: 4 spaces (Java), 2 spaces (XML). No tabs.
- **Line length**: Max 120 characters
- **Javadoc**: Required for all public classes and methods; larger private methods too
- **Test naming**: `*Test.java` = unit tests; `*IT.java` = integration tests
- **Brace style**: K&R (opening brace on same line)
- **License**: All source files must include BSD header (enforced at build time)

See [Code Style Guide](https://wiki.lyrasis.org/display/DSPACE/Code+Style+Guide) for details.

## Critical Constraints

- ❌ Java < 17 → build fails (enforcer plugin)
- ❌ Maven < 3.8 → build fails
- ⚠️ Checkstyle, SpotBugs, and license validation run in build verify phase
- ⚠️ Tests skipped by default—enable with `-DskipUnitTests=false`
- ⚠️ PostgreSQL-only; other databases not supported
- ⚠️ Flyway migrations auto-run on first startup (can be slow)

## Development Workflow

1. **Before coding**: Verify Java 17+, Maven 3.8+, PostgreSQL running
2. **Build locally**: `mvn -pl {module} clean install -DskipTests` to iterate fast
3. **Add tests**: Include `*Test.java` (unit) or `*IT.java` (integration)
4. **Verify code style**: Build must pass Checkstyle; fix with `checkstyle.skip=true` only as last resort
5. **Test REST changes**: Document in [RestContract](https://github.com/DSpace/RestContract)
6. **Add Javadoc**: All public APIs require documentation
7. **Check pull request guidelines**: See [CONTRIBUTING.md](../CONTRIBUTING.md) — PRs should be <1,000 LOC

## Documentation References

- **Code Contribution Guidelines**: https://wiki.lyrasis.org/display/DSPACE/Code+Contribution+Guidelines
- **Code Testing Guide**: https://wiki.lyrasis.org/display/DSPACE/Code+Testing+Guide
- **REST Contract (API docs)**: https://github.com/DSpace/RestContract
- **Release Notes**: https://wiki.lyrasis.org/display/DSDOC9x/Release+Notes

## Common Tasks

### Add a REST Endpoint

1. Create controller method in `dspace-server-webapp/src/.../dspace/app/rest/`
2. Inject service dependencies (e.g., `ItemService`)
3. Add Spring `@RestController`, `@RequestMapping` annotations
4. Include Javadoc for the method
5. Add unit tests to `*Test.java` in parallel structure
6. Document endpoint in RestContract

### Modify Core Business Logic

1. Update service interface in `dspace-api/src/.../service/`
2. Implement change in service class
3. Add corresponding REST controller if client-facing
4. Add/update Javadoc and unit tests
5. Run `mvn -pl dspace-api clean install -DskipTests` locally before full build

### Database Changes

1. Create Flyway migration in `dspace-api/src/main/resources/org/dspace/storage/rdbms/sqlmigration/postgres/`
2. Follow naming: `V{version}__{description}.sql`
3. Test locally against PostgreSQL
4. Document in PR with before/after schema if major

## Debugging Tips

```bash
# Skip quality checks for faster iteration (not for PRs)
mvn clean install -DskipTests -Dcheckstyle.skip=true -Dspotbugs.skip=true

# Increase JVM memory for large builds
export MAVEN_OPTS="-Xmx2g"

# Build only a specific module to verify changes
mvn -pl dspace-server-webapp clean install -DskipTests

# View test reports after failures
cat target/surefire-reports/ (unit)
cat target/failsafe-reports/ (integration)
```

---

**For in-depth project exploration**, see [INDEX.md](../INDEX.md) and [IMPLEMENTATION_SUMMARY.md](../IMPLEMENTATION_SUMMARY.md).
