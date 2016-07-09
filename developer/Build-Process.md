---
layout: default
title: CAS - Build Process
---

# Build Process
This page documents the steps that a CAS developer should take for building a CAS server locally.

## Source Checkout
The following shell commands may be used to grab the source from the repository:

```bash
git clone git@github.com:Jasig/cas.git cas-server
```

## Build
The following shell commands may be used to build the source:

```bash
cd cas-server
./gradlew build --parallel -x test -x javadoc -DskipCheckstyle=true -DskipAspectJ=true -DskipFindbugs=true
```

The following commandline boolean flags are supported by the build:

| Flag                              | Description
|-----------------------------------+----------------------------------------------------+
| `skipCheckstyle`                  | Skip running checkstyle checks.
| `skipAspectJ`                     | Skip decorating source files with AspectJ.
| `skipFindbugs`                    | Skip running findbugs checks.
| `skipVersionConflict`             | If a dependency conflict is found, use the latest version rather than failing the build.
| `genConfigMetadata`               | Generate CAS configuration metadata for `@ConfigurationProperties` classes.


Note that you can use `-x <task>` to entirely skip/ignore a phase in the build. (i.e. `-x test/javadoc`)

## IDE Setup

For IntelliJ IDEA, execute the following commands:

```bash
cd cas-server
./gradlew idea
```

For Eclipse, execute the following commands:

```bash
cd cas-server
./gradlew eclipse
```

## Embedded Tomcat

The CAS project is pre-configured with an embedded Tomcat instance for both the server web application as well as the management web application.

### Configure SSL

The `thekeystore` file must include the SSL private/public keys that are issued for your CAS server domain. You will need to use the `keytool` command of the JDK to create the keystore and the certificate. The following commands may serve as an example:

```bash
keytool -genkey -alias cas -keyalg RSA -validity 999 -keystore /etc/cas/thekeystore
```

Note that the validity parameter allows you to specify, in the number of days, how long the certificate should be valid for. The longer the time period, the less likely you are to need to recreate it. To recreate it, you'd need to delete the old one and then follow these instructions again.

The response will look something like this:

```bash
Enter keystore password: changeit
Re-enter new password: changeit
What is your first and last name?
  [Unknown]:  $REPLACE_WITH_FULL_MACHINE_NAME (i.e. mymachine.domain.edu)
What is the name of your organizational unit?
  [Unknown]:  Test
What is the name of your organization?
  [Unknown]:  Test
What is the name of your City or Locality?
  [Unknown]:  Test
What is the name of your State or Province?
  [Unknown]:  Test
What is the two-letter country code for this unit?
  [Unknown]:  US
Is CN=$FULL_MACHINE_NAME, OU=Test, O=Test, L=Test, ST=Test, C=US correct?
  [no]:  yes
```

### Deploy
Execute the following command:

```bash
CD cas-server-webapp
gradle build bootRun --parallel -x test -DskipCheckstyle=true -x javadoc -DskipAspectJ=true -DskipFindbugs=true 

```

The response will look something like this:

```bash
INFO [org.apache.catalina.core.StandardService] - <Starting service Tomcat>
INFO [org.apache.catalina.core.StandardEngine] - <Starting Servlet Engine: Apache Tomcat/8.0.32>
INFO [org.apache.catalina.core.ContainerBase.[Tomcat].[localhost].[/cas]] - <Initializing Spring embedded WebApplicationContext>
INFO [org.jasig.cas.web.CasWebApplication] - <Started CasWebApplication in 21.485 seconds (JVM running for 22.895)>
```

CAS will be available at `https://mymachine.domain.edu:8443/cas`

### Remote Debugging
The Jetty instance is pre-configured to listen to debugger requests on port `5000`. Create a remote debugger configuration in your IDE that connects to this port and you will be able to step into the code.

### Dependency Updates
CAS integrates with [VersionEye](https://www.versioneye.com/user/projects/5677b4a5107997002d00131b) to report back the version of dependencies used and those that may be outdated.

In order to get a full report on dependencies, adjust the following:

Specify the your VersionEye API key in the `~/.gradle/gradle.properties` file:

```properties
versioneye.api_key=1234567890abcdef
```

Then run the following command at the root:

```bash
gradle versionEyeUpdate
```

Browse the report at the above link to see which dependencies might need attention. 
