CAS Spring Cloud Configuration Server Overlay Template
============================

Generic CAS Spring Cloud Configuration Server WAR overlay.

# Versions

- CAS Server `@casVersion@`
- JDK `{{javaVersion}}

# Deployment

To build the project, use:

```bash
# Use --refresh-dependencies to force-update SNAPSHOT versions
./gradlew[.bat] clean build
```

To see what commands/tasks are available to the build script, run:

```bash
./gradlew[.bat] tasks
```

On a successful deployment via the following methods, the configuration server will be available at:

* `https://cas.server.name:8888/casconfigserver`

## Executable WAR

Run the configuration server web application as an executable WAR.

```bash
java -Xdebug -Xrunjdwp:transport=dt_socket,address=5000,server=y,suspend=n -jar build/libs/casconfigserver.war 
```

## External

Deploy resultant `build/libs/casconfigserver.war` to a servlet container of choice.
