---
layout: default
title: CAS - High Availability Guide
---
# High Availability Guide (HA/Clustering)

The choice of CAS components and their configuration may be influenced by system design considerations such as operating environment (including network), performance and availability requirements, and system integration needs. Clustering and related kinds of redundancy are one of the most common high-level requirements of CAS, and the details of the design, for example stateful versus stateless, will impose various requirements on CAS component choice and configuration.

## Baseline Recommendation

We recommend the following guidelines as a starting point for a highly available CAS deployment.

### Multiple CAS Server Nodes
A HA CAS setup is composed of at least two CAS nodes behind a hardware load balancer in either active/active or active/passive mode. While active/active setups may have additional requirements for some storage backends (e.g. Ehcache), they generally provide better resource allocation and fewer or shorter interruptions of service. There are notable exceptions to that generalization, but it holds for most cases.

### Session Affinity
In order to provide for proper state management during the CAS authentication process, session affinity between client browser and CAS server nodes is required. While it is possible to achieve truly stateless clusters by plugging in [client-based state management](https://github.com/serac/spring-webflow-client-repo) components, such configurations are complex and offer trade-offs that are outside the scope of this document. It is important to node that session affinity is _not_ required between connections from CAS client applications and the CAS server for ticket validation as there is no client-server state managed by that interaction.

### Cache-based Storage Backend
The following cache-based ticket storage components provide the best tradeoff among ease of use, scalability, and robustness:
* EhCacheTicketRegistry
* MemcachedTicketRegistry
* JBossTicketRegistry

### Connection Pooling
We _strongly_ recommend that all IO connections to a back-end data stores, such as LDAP directories and databases, leverage connection pooling where possible. It makes the best use of computational (especially when connection startup involves SSL/TLS) and IO resources while providing the best performance characteristics.
