---
layout: default
title: CAS - Ignite Ticket Registry
---

# Ignite Ticket Registry
Ignite integration is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-ignite-ticket-registry</artifactId>
     <version>${cas.version}</version>
</dependency>
```

This registry stores tickets in an [Ignite](http://ignite.apache.org/) instance.


## Distributed Cache
Distributed caches are recommended for HA architectures since they offer fault tolerance in the ticket storage subsystem.


## TLS Replication

Ignite supports replication over TLS for distributed caches composed of two or more nodes. To learn more about TLS replication with Ignite,
[see this resource](https://apacheignite.readme.io/docs/ssltls).

For test environments, TLS certificate verification may be disabled by setting `ignite.trustStoreFilePath` *and*
`ignite.trustStorePassword` to `NULL`

Additional TLS context configuration if performed by setting the following properties. 
In almost all cases, the Ignite defaults should work.

## Configuration

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

## Troubleshooting Guidelines

* You will need to ensure that network communication across CAS nodes is allowed and no firewall or other component is blocking traffic.
* If nodes external to CAS instances are utilized, ensure that each cache manager specified a name that matches the Ignite configuration
  itself.
* You may also need to adjust your expiration policy to allow for a larger time span, specially for service tickets depending on network
  traffic and communication delay across CAS nodes particularly in the event that a node is trying to join the cluster.
