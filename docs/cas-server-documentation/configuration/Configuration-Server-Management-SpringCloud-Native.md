---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud Native

Spring Cloud Configuration Server is configured by default to load `cas.(properties|yml)` files from an external location that is `/etc/cas/config`.
This location is constantly monitored by the server to detect external changes. Note that this location needs to
exist, and does not require any special permissions or structure. The name of the configuration file that goes inside this
directory needs to match the `spring.application.name` (i.e. `cas.properties`).

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.config.server.native" %}

If you want to use additional configuration files, they need to have the
form `application-<profile>.(properties|yml)`.
A file named `application.(properties|yml)` will be included by default. The profile specific
files can be activated by using the `spring.profiles.include` configuration option,
controlled via the `src/main/resources/bootstrap.properties` file:

```properties
spring.profiles.active=native
spring.cloud.config.server.native.search-locations=file:///etc/cas/config
spring.profiles.include=profile1,profile2
```

An example of an external `.properties` file hosted by an external location follows:

```properties
cas.server.name=...
```

You could have just as well used a `cas.yml` file to host the changes. Note that
the default profile is activated using `spring.profiles.active=native`.
