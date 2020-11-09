# CAS Initializer

CAS Initializr provides a build system and an API to dynamically generate 
CAS overlays. The project is a based on [Spring Initializer](https://github.com/spring-io/initializr).

## Build

You will need JDK 11 to run the CAS Initializer locally.

```bash
./gradlew clean build
```                  

Generate a Docker image from the build:

```bash
./gradlew bootBuildImage
```

## Run

CAS Initializer is a Spring Boot application and can be run using the following command:

```bash
./gradlew :app:bootRun
```

Or run the Docker image:

```bash  
imageTag=(`./gradlew casVersion --q`) && docker run --rm -p 8080:8080 -t apereo/cas-initializr:$imageTag 
```

The service will be available on `http://localhost:8080`.

## Generating a project

Generate a vanilla CAS overlay:

```bash
curl http://localhost:8080/starter.zip -o cas.zip
```

Generate a CAS overlay package as a compressed tarball 
that contains indicated modules/dependencies selected by their identifier:

```bash
curl http://localhost:8080/starter.tgz -d dependencies=core | tar -xzvf -
```

## Dependency List

Dependencies that can be requested must be specified by their identifier. To see a full list of
all dependencies supported and available by this service, you can invoke the following command:

```bash
curl http://localhost:8080/dependencies
```

Typically, dependency identifiers match CAS server 
dependency/module artifact names without the `cas-server-` prefix.

## Service metadata

The metadata lists the capabilities of the service, 
that is the available options for all request parameters 
(dependencies, type, bootVersion, etc.) A client to the service 
uses that information to initialize the select options and the tree of available dependencies.

You can grab the metadata on the root endpoint with the appropriate Accept header:

```bash
curl -H 'Accept: application/json' http://localhost:8080
```     

Or using `HTTPie`:

```bash
http http://localhost:8080 Accept:application/json
```
