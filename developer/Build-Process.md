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

The following commandline flags are supported by the build:

| Flag                              | Description
|-----------------------------------+----------------------------------------------------+
| `skipCheckstyle`                  | Skip running checkstyle checks.
| `skipTests`                       | Skip running JUnit tests, but compile them.
| `skipAspectJ`                     | Skip decorating source files with AspectJ.
| `skipFindbugs`                    | Skip running findbugs checks.
| `skipVersionConflict`             | If a dependency conflict is found, use the latest version rather than failing the build.

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

## Embedded Jetty

The CAS project is pre-configured with an embedded Jetty instance for both the server web application as well as the management web application.

### Configure SSL

Edit `$USER_HOME/.gradle/gradle.properties` to include the following:

```properties
jettySslKeyStorePath=/etc/cas/jetty/thekeystore
jettySslKeyStorePassword=changeit
jettySslTrustStorePath=/etc/cas/jetty/thekeystore
jettySslTrustStorePassword=changeit
```

The `thekeystore` file must include the SSL private/public keys that are issued for your CAS server domain. You will need to use the `keytool` command of the JDK to create the keystore and the certificate. The following commands may serve as an example:

```bash
keytool -genkey -alias cas -keyalg RSA -validity 999 -keystore /etc/cas/jetty/thekeystore
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
gradle build jettyRunWar --parallel -x test -DskipCheckstyle=true -x javadoc -DskipAspectJ=true -DskipFindbugs=true --console rich
```

The response will look something like this:

```bash
05:52:06 INFO  Initializing Spring FrameworkServlet 'cas'
05:52:06 INFO  Jetty 9.2.10.v20150310 started and listening on ports 8080, 8443
05:52:06 INFO  Central Authentication System (CAS) runs at:
05:52:06 INFO    http://localhost:8080/cas
05:52:06 INFO    https://localhost:8443/cas
```

CAS will be available at `https://mymachine.domain.edu:8443/cas`

### Remote Debugging
The Jetty instance is pre-configured to listen to debugger requests on port `5000`. Create a remote debugger configuration in your IDE that connects to this port and you will be able to step into the code.
