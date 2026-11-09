---
layout: default
title: CAS - Spring Cloud Configuration Server - Spring Cloud Native
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud Native

Spring Cloud Configuration Server is configured by default to load `cas.(properties|yml)` files from an external location that is `/etc/cas/config`.
This location is constantly monitored by the server to detect external changes. Note that this location needs to
exist, and does not require any special permissions or structure. The name of the configuration file that goes inside this
directory needs to match the application name (i.e. `cas.properties`).

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.config.server.native" thirdPartyExactMatch="spring.profiles.active,spring.profiles.include" %}

If you want to use additional configuration files, they need to have the
form `application-<profile>.(properties|yml)`.
A file named `application.(properties|yml)` will be included by default. The profile specific
files are activated by including their profiles, such as `profile1,profile2`, in the `src/main/resources/bootstrap.properties`
file, where the `native` profile is active and the search locations of the server point at `file:///etc/cas/config`.

An external `.properties` file hosted by an external location holds CAS settings as usual. You could have just as well
used a `cas.yml` file to host the changes. Note that the default profile here is the `native` profile.
