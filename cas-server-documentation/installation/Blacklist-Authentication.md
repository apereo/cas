---
layout: default
title: CAS - Blacklist Authentication
---

# Blacklist Authentication
Blacklist authentication components are those that specifically deny access to a set of credentials.
Those that fail to match against the predefined set will blindly be accepted.

These are:

* `RejectUsersAuthenticationHandler`

## Authentication Components
Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-generic</artifactId>
  <version>${cas.version}</version>
</dependency>
```

### `RejectUsersAuthenticationHandler` in `application.properties`:

```properties
primaryAuthenticationHandler=rejectUsersAuthenticationHandler
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).
