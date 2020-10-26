Apereo CAS Overlay Template
=======================

CAS Overlay to use as a starting template for CAS deployments.

# Versions

- CAS `{{version}}`
- JDK `{{javaVersion}}`
- Spring Boot `{{bootVersion}}`

# Build

To build the project, use:

```bash
# Use --refresh-dependencies to force-update SNAPSHOT versions
./gradlew[.bat] clean build
```

To see what commands/tasks are available to the build script, run:

```bash
./gradlew[.bat] tasks
```

## CAS Command-line Shell

To launch into the CAS command-line shell:

```bash
./gradlew[.bat] downloadShell runShell
```

## Retrieve Overlay Resources

To fetch and overlay a CAS resource or view, use:

```bash
./gradlew[.bat] getResource -PresourceName=[resource-name]
```

## Create User Interface Themes Structure

You can use the overlay to construct the correct directory structure for custom user interface themes:

```bash
./gradlew[.bat] createTheme -Ptheme=redbeard
```

The generated directory structure should match the following:

```
├── redbeard.properties
├── static
│   └── themes
│       └── redbeard
│           ├── css
│           │   └── cas.css
│           └── js
│               └── cas.js
└── templates
    └── redbeard
        └── fragments
```

HTML templates and fragments can be moved into the above directory structure, and the theme may be assigned to applications for use.

## List Overlay Resources
 
To list all available CAS views and templates:

```bash
./gradlew[.bat] listTemplateViews
```

To unzip and explode the CAS web application file and the internal resources jar:

```bash
./gradlew[.bat] explodeWar
```

# Configuration

- The `etc` directory contains the configuration files and directories that need to be copied to `/etc/cas/config`.

```bash
./gradlew[.bat] copyCasConfiguration
```

- The specifics of the build are controlled using the `gradle.properties` file.

## Configuration Metadata

Configuration metadata allows you to export collection of CAS properties as a report into a file 
that can later be examined. You will find a full list of CAS settings along with notes, types, default and accepted values:

```bash
./gradlew exportConfigMetadata
```                           

## Adding Modules

CAS modules may be specified under the `dependencies` block of the [Gradle build script](build.gradle):

```gradle
dependencies {
    implementation "org.apereo.cas:cas-server-some-module"
    ...
}
```

To collect the list of all project modules and dependencies in the overlay:

```bash
./gradlew[.bat] allDependencies
```                                                                       

To see a full list of all project dependencies that are available for configuration and use:

```bash
curl {{initializrUrl}}/dependencies
```     

Or:

```bash
curl {{initializrUrl}}/actuator/info
```


### Clear Gradle Cache

If you need to, on Linux/Unix systems, you can delete all the existing artifacts 
(artifacts and metadata) Gradle has downloaded using:

```bash
# Only do this when absolutely necessary
rm -rf $HOME/.gradle/caches/
```

Same strategy applies to Windows too, provided you switch `$HOME` to its equivalent in the above command.

# Deployment

- Create a keystore file `thekeystore` under `/etc/cas`. Use the password `changeit` for both the keystore and the key/certificate entries. This can either be done using the JDK's `keytool` utility or via the following command:

```bash
./gradlew[.bat] createKeystore
```

- Ensure the keystore is loaded up with keys and certificates of the server.

On a successful deployment via the following methods, CAS will be available at:

* `https://cas.server.name:8443/cas`

## Executable WAR

Run the CAS web application as an executable WAR:

```bash
./gradlew[.bat] run
```

Debug the CAS web application as an executable WAR:

```bash
./gradlew[.bat] debug
```

Run the CAS web application as a *standalone* executable WAR:

```bash
./gradlew[.bat] clean executable
```

## External

Deploy the binary web application file `cas.war` after a successful build to a servlet container of choice.

## Docker

The following strategies outline how to build and deploy CAS Docker images.

### Spring Boot

You can build a container image using the [Spring Boot Gradle build plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/). This approach does not require a Dockerfile. You build the image 
using the same standard container format as you get from `docker build` - and it can work in environments where docker is not installed.
This task requires access to a Docker daemon. By default, it will communicate with a Docker daemon over a local connection.

```bash
./gradlew build bootBuildImage
```

### Jib

The overlay embraces the [Jib Gradle Plugin](https://github.com/GoogleContainerTools/jib) to provide easy-to-use out-of-the-box tooling for building CAS docker images. Jib is an open-source Java containerizer from Google that lets Java developers build containers using the tools they know. It is a container image builder that handles all the steps of packaging your application into a container image. It does not require you to write a Dockerfile or have Docker installed, and it is directly integrated into the overlay.

```bash
./gradlew build jibDockerBuild
```

### Dockerfile

You can also use the native Docker tooling and the provided `Dockerfile` to build and run CAS.

```bash
chmod +x *.sh
./docker-build.sh
./docker-run.sh
```

### Docker Compose

For convenience, an additional `docker-compose.yml` is also provided to orchestrate the build:

```bash  
docker-compose build
```
