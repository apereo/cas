---
layout: default
title: CAS - Build Process
---

# Build Process

This page documents the steps that a CAS developer/contributor should take for building a CAS server locally.

## Development

CAS development is powered by: <br/>

<a href="http://www.jetbrains.com/idea/" target="_blank"><img src="../images/intellijidea.gif" width="100" height="100" valign="middle" style="vertical-align:middle"></a>

## Source Checkout

The following shell commands may be used to grab the source from the repository:

```bash
git clone git@github.com:apereo/cas.git cas-server
```

Or a quicker clone:

```bash
git clone --depth=1 --single-branch --branch=master git@github.com:apereo/cas.git cas-server
# git fetch --unshallow
```

For a successful clone, you will need to have [set up SSH keys](https://help.github.com/articles/working-with-ssh-key-passphrases/) for your account on Github.
If that is not an option, you may clone the CAS repository under `https` via `https://github.com/apereo/cas.git`.

## Build

The following shell commands may be used to build the source:

```bash
cd cas-server
git checkout master
```

When done, you may build the codebase via the following command:

```bash
./gradlew build install --parallel -x test -x javadoc -x check
```

The following commandline boolean flags are supported by the build:

| Flag                              | Description
|-----------------------------------+------------------------------------------------------------------+
| `skipCheckstyle`                  | Skip running checkstyle checks.
| `enableRemoteDebugging`           | Allows for remote debugging via a pre-defined port (i.e. `5000`).
| `skipFindbugs`                    | Skip running findbugs checks.
| `skipVersionConflict`             | If a dependency conflict is found, use the latest version rather than failing the build.
| `showStandardStreams`             | Let the build output logs that are sent to the standard streams. (i.e. console, etc)
| `enableIncremental`               | Enable Gradle's incremental compilation feature.
| `enableKotlin`                    | Enable compilation of Kotlin's `.kt` files, if any. 
| `enableJRebelAgent`               | Enable [JRebel support](https://zeroturnaround.com/software/jrebel/). Requires the environment variable `JREBEL_LIB_PATH` to point to the appropriate JRebel library path (DLL, etc).

- You can use `-x <task>` to entirely skip/ignore a phase in the build. (i.e. `-x test`, `-x check`).
- If you have no need to let Gradle resolve/update dependencies and new module versions for you, you can take advantage of the `--offline` flag when you build which tends to make the build go a lot faster.
- Using the Gradle daemon also is a big help. [It should be enabled by default](https://docs.gradle.org/current/userguide/gradle_daemon.html).
- Enabling [Gradle's build cache](https://docs.gradle.org/current/userguide/build_cache.html) via `--build-cache` can also significantly improve build times.

## Tasks

Available build tasks can be found using the command `./gradlew tasks`.

### Sass Compilation

The build is automatically wired to compile `.scss` files into `.css` via a [Gulp](http://gulpjs.com/).
To let this step successfully pass, you may need to install `gulp` and `npm`, which the build should automatically do.
See the **Build** section for more info.

## IDE Setup

CAS development may be carried out using any modern IDE. 

### IntelliJ IDEA

For IntelliJ IDEA, execute the following commands:

```bash
cd cas-server
./gradlew idea
```

Then, open the project as you would for any other project and let IDEA resolve the Gradle dependencies. 

The following IDEA settings for Gradle may also be useful:

![image](https://cloud.githubusercontent.com/assets/1205228/23250938/68e0b7ac-f9c0-11e6-9ce1-cb1fae07c6ae.png)

- Note how 'Use auto-import' is turned off. To resolve Gradle modules and dependencies, you are required to force refresh the project rather than have IDEA auto-refresh the project as you make changes to the build script. Disabling auto-import usually results in much better performance.
- Note how 'Offline work' is enabled. This is equivalent to Gradle's own `--offline` flag, forcing the build to not contact Maven/Gradle repositories for resolving dependencies. Working offline usually results in much better performance.
- You may also decide to use the `default gradle wrapper' option as opposed to your own local Gradle installation. 

You may also need to adjust the 'Compiler' settings so modules are built in parallel and automatically:

![image](https://cloud.githubusercontent.com/assets/1205228/23251099/31d8f250-f9c1-11e6-9ca1-64489bc1a948.png)

### Eclipse

For Eclipse, execute the following commands:

```bash
cd cas-server
./gradlew eclipse
```

Then, open the project as you would for any other project. 

<div class="alert alert-warning"><strong>YMMV</strong><p>We have had a less than ideal experience with Eclipse and its support for Gradle-based projects. While time changes everything and docs grow old, it is likely that you may experience issues with how Eclipse manages to resolve Gradle dependencies and build the project. In the end, you're welcome to use what works best for you as the ultimate goal is to find the appropriate tooling to build and contribute to CAS.</p></div>

## Testing Modules

To test the functionality provided by a given CAS module, execute the following steps:

- Add the module reference to the build script (i.e. `build.gradle`) of web application you intend to run (i.e Web App, Management Web App, etc)

```gradle
compile project(":support:cas-server-support-modulename")
```

- Prepare the embedded container, as described below, to run and deploy the web application

## Embedded Containers

The CAS project comes with a number of built-in modules that are pre-configured with embedded servlet containers such as Apache Tomcat, Jetty, etc 
for the server web application, the management web application and others.

### Configure SSL

The `thekeystore` file must include the SSL private/public keys that are issued for your CAS server domain. You will need to use the `keytool` command of the JDK to create the keystore and the certificate. 
The following commands may serve as an example:

```bash
keytool -genkey -alias cas -keyalg RSA -validity 999 -keystore /etc/cas/thekeystore -ext san=dns:$REPLACE_WITH_FULL_MACHINE_NAME
```

Note that the validity parameter allows you to specify, in the number of days, how long the certificate should be valid for. The longer the time period, the less likely you are to need to recreate it. To recreate it, you'd need to delete the old one and then follow these instructions again. You may also need to provide the *Subject Alternative Name* field, which can be done with `keytool` via `-ext san=dns:$REPLACE_WITH_FULL_MACHINE_NAME`.

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

In your `/etc/hosts` file (on Windows: `C:\Windows\System32\Drivers\etc\hosts`), you may also need to add the following entry:

```bash
127.0.0.1 mymachine.domain.edu
```

The certificate exported out of your keystore needs to also be imported into the Java platform's global keystore:

```bash
# Export the certificate into a file
keytool -export -file /etc/cas/config/cas.crt -keystore /etc/cas/thekeystore -alias cas

# Import the certificate into the global keystore
sudo keytool -import -file /etc/cas/config/cas.crt -alias cas -keystore $JAVA_HOME/jre/lib/security/cacerts
```

...where `JAVA_HOME` is where you have the JDK installed (i.e `/Library/Java/JavaVirtualMachines/jdk[version].jdk/Contents/Home`).

### Deploy

Execute the following command:

```bash
cd webapp/cas-server-webapp-tomcat

# Or for the management-webapp:
# cd webapp-mgmt/cas-management-webapp

../../gradlew build bootRun --parallel --offline
```

The response will look something like this:

```bash
...
2017-05-26 19:10:46,470 INFO [org.apereo.cas.web.CasWebApplication] - <Started CasWebApplication in 21.893 seconds (JVM running for 36.888)>
...
```

By default CAS will be available at `https://mymachine.domain.edu:8443/cas`

### Remote Debugging

The embedded container instance is pre-configured to listen to debugger requests on port `5000` provided you specify the `enableRemoteDebugging` parameter. 
For external container deployments, [such as Apache Tomcat](https://wiki.apache.org/tomcat/FAQ/Developing#Q1), 
the following example shows what needs configuring in the `bin/startup.sh|bat` file:

```bash
export JPDA_ADDRESS=5000
export JPDA_TRANSPORT=dt_socket
bin/catalina.sh jpda start
```

When you're done, create a remote debugger configuration in your IDE that connects to this port and you will be able to step into the code.

![image](https://cloud.githubusercontent.com/assets/1205228/26517058/d09a8288-4245-11e7-962e-004bfe174a0a.png)

### Dependency Updates

CAS integrates with [VersionEye](https://www.versioneye.com/user/projects/5677b4a5107997002d00131b) to 
report back the version of dependencies used and those that may be outdated.

In order to get a full report on dependencies, adjust the following:

Specify the your VersionEye API key in the `~/.gradle/gradle.properties` file:

```properties
versioneye.api_key=1234567890abcdef
```

Then run the following command at the root:

```bash
./gradlew versionEyeUpdate
```

Browse the report at the above link to see which dependencies might need attention. 

## Continuous Integration

CAS uses [Travis CI](https://travis-ci.org/apereo/cas/builds) as its main continuous integration tool. The build primaryly is
controlled by the `.travis.yml` file, defined at the root of the project directory. 

The following special commit messages are recognized by Travis CI to control aspects
of build behavior:

| Commit Message                    | Description
|-----------------------------------+------------------------------------------------------------------+
| `[skip ci]`                       | Skip running a build completely.
| `[skip tests]`                    | Skip running tests.

Travis CI is mainly responsible for the following tasks:

- Running a full build, including tests and style checks.
- Pushing project documentation artifacts into the `gh-pages` branch.
- Uploading snapshots to relevant repositories.

The build is triggered for automatically for all pull requests, direct commits, etc where different
policies may apply for each change type.
