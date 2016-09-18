---
layout: default
title: CAS - MongoDb Authentication
---

# MongoDb Authentication

Verify and authenticate credentials against a [MongoDb](https://www.mongodb.org/) instance.
Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-mongo</artifactId>
  <version>${cas.version}</version>
</dependency>
```

The following settings are applicable:

To see the relevant list of CAS properties, 
please [review this guide](Configuration-Properties.html).
