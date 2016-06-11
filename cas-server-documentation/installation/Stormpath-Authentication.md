---
layout: default
title: CAS - Stormpath Authentication
---

# Stormpath Authentication
Verify and authenticate credentials against the [Stormpath](https://stormpath.com/) Cloud Identity.

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-stormpath</artifactId>
  <version>${cas.version}</version>
</dependency>
```

In `application.properties`:

```properties

primaryAuthenticationHandler=stormpathAuthenticationHandler
```

The following settings are applicable:

```properties
# cas.authn.stormpath.apiKey=
# cas.authn.stormpath.applicationId=
# cas.authn.stormpath.secretkey=
```
