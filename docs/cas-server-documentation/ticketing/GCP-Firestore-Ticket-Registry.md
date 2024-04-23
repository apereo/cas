---
layout: default
title: CAS - GCP Firestore Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Google Cloud Firestore Ticket Registry

[Firestore](https://cloud.google.com/firestore/docs) is a NoSQL document database built for automatic scaling, high 
performance, and ease of application development. While the Firestore interface has many of the same features as traditional 
databases, as a NoSQL database it differs from them in the way it describes relationships between data objects.

Support is enabled by including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gcp-pubsub-ticket-registry" %}

Integration support is backed by the [Spring Cloud GCP project](https://cloud.google.com/java/docs/spring).
Their [reference documentation](https://googlecloudplatform.github.io/spring-cloud-gcp/reference/html/index.html) 
provides detailed information on how to integrate Google Cloud APIs with CAS.
     
Note that CAS will automatically create the appropriate collections required for each ticket type. However, TTL expiration policies
and field indexes for advanced use cases are not created automatically by CAS and may require manual intervention.
        
To understand how to set up application default credentials, please [review this page](https://cloud.google.com/docs/authentication/application-default-credentials).

## CAS Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.google-cloud-firestore" thirdPartyStartsWith="spring.cloud.gcp.firestore" %}

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.google.cloud" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
