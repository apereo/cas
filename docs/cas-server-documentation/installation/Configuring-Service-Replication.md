---
layout: default
title: CAS - Configuring Service Replication
---

# Configure Service Replication

In the event that CAS service definitions are not managed globally via a centralized store, 
definitions need to be kept in sync throughout all CAS nodes in a cluster when more than one node is deployed. 
When the management strategy of such definitions is to store them on disk local to each node (such as JSON or YAML) files, 
the following mechanisms may be used to copy files from one host to another.

## Native

A background task can be scheduled with the likes of `rsync` to copy files from from host to another. 
The job needs to of course run periodically to ensure configuration is kept in sync. 
This is the simplest option as CAS is completely ignorant of extra process in the background.

## Hazelcast

If you'd rather not resort to outside tooling and processes or if the native options for your 
deployment are not that attractive, you can take advantage of CAS' own tooling that provides a 
distributed cache to broadcast service definition files across the cluster and add/remove/update 
each node as needed. As service definitions are loaded by CAS, events are broadcasted to all 
CAS nodes in the cluster to pick up the changes and keep definitions in sync. 

Support is enabled by including the following dependency in the overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-service-registry-stream-hazelcast</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, 
please [review this guide](Configuration-Properties.html#service-registry-replication-hazelcast).
