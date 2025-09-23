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
    
To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% include_cached casproperties.html properties="cas.audit.groovy" %}

## Scripts

The following parameters are passed to the script:

| Parameter            | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `applicationContext` | The object representing the Spring application context.                     |
| `logger`             | The object responsible for issuing log messages such as `logger.info(...)`. |
| `clientIpAddress`    | self-explanatory.                                                           |
| `serverIpAddress`    | self-explanatory.                                                           |
| `what`               | self-explanatory.                                                           |
| `who`                | self-explanatory.                                                           |
| `when`               | self-explanatory.                                                           |
| `action`             | self-explanatory.                                                           |
| `userAgent`          | self-explanatory.                                                           |
| `application`        | self-explanatory.                                                           |
| `geoLocation`        | self-explanatory.                                                           |
| HTTP Request headers | All collected headers are passed by their name.                             |
| *Extra Info*         | Arbitrary keys/names collected by the audit engine from various components. |

A sample script follows:

```groovy
${logger.info("Hello, World")}

who: ${who}, what: ${what}, when: ${when}, ip: ${clientIpAddress}, trace: ${customHttpRequestHeader}
```
