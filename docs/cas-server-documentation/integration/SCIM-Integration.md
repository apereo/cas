---
layout: default
title: CAS - SCIM Provisioning Integration
---

# Overview

The [SCIM standard](http://www.simplecloud.info/) is created to simplify user management and provisioning in the cloud by defining a schema 
for representing users and groups and a REST API for all the necessary CRUD operations. SCIM integrations with CAS allow deployers
to auto-provision the authenticated CAS principal to a SCIM server/target with additional support to map principal attributes into the
appropriate claims and properties of the user resource.

SCIM versions 1.1 and 2 are both supported, thanks to the SDK provided by [UnboundID](https://github.com/UnboundID).

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-scim</artifactId>
  <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](../installation/Configuration-Properties.html).
