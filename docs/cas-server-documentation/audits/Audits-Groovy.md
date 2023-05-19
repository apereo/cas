---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# Groovy Audits

Groovy-based audits have the ability to receive and process the auditable context parameters
and build the final auditable record in any text format or representation. 
The final auditable record is then passed to the logging framework, typically tagged under `INFO`.

{% include_cached casproperties.html properties="cas.audit.groovy" %}

## Scripts

The following parameters are passed to the script:

| Parameter         |
|-------------------|
| `clientIpAddress` |
| `serverIpAddress` |
| `what`            |
| `who`             |
| `when`            |
| `action`          |
| `userAgent`       |
| `application`     |
| `headers`         |

A sample script follows:

```groovy
who: ${who}, what: ${what}, when: ${when}, ip: ${clientIpAddress}
```
