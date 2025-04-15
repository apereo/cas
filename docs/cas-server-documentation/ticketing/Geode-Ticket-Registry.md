---
layout: default
title: CAS - Apache Geode Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Apache Geode Ticket Registry

Apache Geode integration is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-geode-ticket-registry" %}

This registry stores tickets in an [Apache Geode](http://geode.apache.org/) instance. On startup, CAS
created dedicated regions in `REPLICATED` mode for each ticket type. Additional indexes are created
on ticket documents stored to improve performance.
        
## Replication 

With `REPLICATED` mode, the entire dataset in that region is copied and stored on every participating member 
that hosts the region. Any change made to the region (such as a put or update) is propagated to all members. Since every 
member holds an identical copy of the data, read operations can be performed locally on any member, 
and the data is consistent across the cluster. This makes replicated regions particularly useful 
for read-intensive applications where having low-latency, local access to all the data is important.
                           
This option provides the following advantages:

- As every member has the complete dataset, reads are extremely fast because they occur locally without the need for inter-node communication.
- Data is available on all nodes. If one node fails, other nodes still have the entire dataset, ensuring high availability.
- Queries that involve looking up data from the entire dataset are simplified because the full data is present locally on each member.

You should also note that with this mode:
  
- Since the data is fully replicated on every node, the memory footprint multiplies with the number of nodes. This approach is best suited for datasets that are moderate in size.
- Every write operation (such as an update or insert) must be propagated to all members, which can incur additional network overhead and latency, especially as the size of the cluster increases.
- `REPLICATED` mode is most beneficial in scenarios where the workload is primarily read-heavy and the size of the dataset remains manageable.

## Configuration

{% include_cached casproperties.html properties="cas.ticket.registry.geode" %}
