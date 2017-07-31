---
layout: default
title: CAS Configuration Metadata Repository
---

# Configuration Metadata

CAS ships with meta-data files that provide details of all supported configuration properties and settings. The repository of all configuration metadata
is generated automatically at build and release time by processing all items annotated with `@ConfigurationProperties` within the codebase. This repository
is then made available for additional querying and filtering to look up definitions of a given property or locate relevant settings that apply to a 
particular group of functionality in CAS, such as LDAP authentication.

## Metadata via REST

Configuration metadata can be queried via the following REST endpoint that are prefixed at `/cas/config/metadata`:

| Endpoint              | Method      | Description
|-----------------------|-------------|----------------------------------------------------------
| `/properties`         | `GET`       | List all properties available in the metadata repository.
| `/groups`             | `GET`       | List all groups available in the metadata repository. 
| `/group`              | `GET`       | Look up a group by its name using a `name` request parameter.
| `/property`           | `GET`       | Look up a property by its name using a `name` request parameter.

This interface ships with CAS by default and you need not do anything special to configure it.

## Metadata via CLI

The metadata repository can also be examined using the command-line as a separate utility. 
The functionality is contained with a self-contained CAS module/JAR
that is `cas-server-core-configuration-metadata-server` which presents a command-line 
interface as well as an interactive shell allowing you to query settings and groups.

To invoke and work with the repository, simply execute:

```bash
java -jar /path/to/cas-server-core-configuration-metadata-server-$casVersion.jar
```

...where `$casVersion` needless to say is the CAS version that is deployed.

The interface that is next presented will guide you through with available parameters and methods of querying.
Using the same approach, you will also learn how to launch into the interactive shell and query the metadata
engine dynamically.

Note that the [WAR Overlay deployment strategy](Maven-Overlay-Installation.html) should already be equipped with the metadata server.
You should not have to do anything special and extra to interact with the metadata repository. 
