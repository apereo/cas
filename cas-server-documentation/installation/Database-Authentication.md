---
layout: default
title: CAS - Database Authentication
---

# Database Authentication

Database authentication is enabled by including the following dependencies in the WAR overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-jdbc</artifactId>
    <version>${cas.version}</version>
</dependency>
```

## Configuration

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).
