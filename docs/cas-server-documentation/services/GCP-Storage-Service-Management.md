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
     
## Google Cloud Storage with Pub/Sub Listener

CAS allows the Google Cloud Storage-backed service registry to listen for object-change 
events published through Google Pub/Sub. When a service definition or related registry object is created, 
updated, or deleted in the configured GCS bucket, the listener receives the notification and can refresh 
or invalidate the local registry state accordingly.

This is useful in clustered or cloud-native deployments where multiple CAS nodes may rely on 
the same GCS bucket as the source of truth. Instead of requiring restarts, polling, or manual 
refreshes, registry changes can be propagated to running nodes as events occur, helping keep 
service definitions synchronized across the deployment with lower latency and less operational overhead.

The subscription/topic name is set to be `cas-service-regisry-gcp-storage`. For the CAS listener to operate, the 
required Pub/Sub infrastructure must exist ahead of time. The Cloud Storage service account for the project 
must also have permission to publish messages to the topic. CAS only consumes messages from the configured 
subscription; it does not automatically create the topic, subscription, bucket notification, or IAM bindings.

You may configure the bucket to emit supported events such as `OBJECT_FINALIZE` and `OBJECT_DELETE`.

{% include_cached featuretoggles.html features="ServiceRegistry.gcp-storage-pubsub" %}
          
Event payloads are expected to match the following structure:

```json
{
  "eventType": "OBJECT_FINALIZE",
  "bucketId": "...",
  "objectId": "...",
  "objectGeneration": "1710000000000000",
  "eventTime": "2025-06-13T17:22:30.000Z",
  "payloadFormat": "JSON_API_V1",
  "notificationConfig": "..."
}
```

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

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](AutoInitialization-Service-Management.html) for more info.

