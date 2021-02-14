CAS Management Overlay
============================

CAS management web application WAR overlay for CAS with externalized configuration.

# Versions

- CAS Management `@casMgmtVersion@`
- CAS Server `@casMgmtCasVersion@`
- JDK `{{javaVersion}}`

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

## Embedded Tomcat
   
```bash
java -Xdebug -Xrunjdwp:transport=dt_socket,address=5000,server=y,suspend=n -jar build/libs/cas-management.war 
```

CAS will be available at:

* `http://mgmt.server.name:8080/cas-management`
* `https://mgmt.server.name:8443/cas-management`

## External

Deploy resultant `target/cas-management.war` to a servlet container of choice.

### Dockerfile

You can also use the native Docker tooling and the provided `Dockerfile` to build and run CAS Management.

```bash
chmod +x *.sh
./docker-build.sh
./docker-run.sh
```
