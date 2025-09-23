---
layout: default
title: CAS - GCP Storage Service Registry
category: Services
---

{% include variables.html %}

# Google Cloud Storage Service Registry

[Google Cloud Storage](https://cloud.google.com/storage) is a managed service for storing unstructured data. CAS takes
advantage of Google Cloud Storage to store service and application definitions. Applications are grouped based on type
and are stored in their own bucket, which are managed and/or created automatically.

Support is enabled by including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gcp-storage-service-registry" %}

Integration support is backed by the [Spring Cloud GCP project](https://cloud.google.com/java/docs/spring).
Their [reference documentation](https://googlecloudplatform.github.io/spring-cloud-gcp/reference/html/index.html) 
provides detailed information on how to integrate Google Cloud APIs with CAS.
        
To understand how to set up application default credentials, 
please [review this page](https://cloud.google.com/docs/authentication/application-default-credentials).

## CAS Configuration

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.gcp.storage" %}

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
