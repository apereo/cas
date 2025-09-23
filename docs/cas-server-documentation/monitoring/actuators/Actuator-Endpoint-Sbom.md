---
layout: default
title: CAS - Monitoring & Statistics
category: Monitoring & Statistics
---

{% include variables.html %}

# Actuator Endpoint - SBOM

The `sbom` endpoint provides information about the software bill of materials (SBOM).

{% include_cached actuators.html endpoints="sbom" %}

The resulting response is similar to the following:

```
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v3+json
Content-Length: 31

{
    "ids" : [ "application" ]
}
```
