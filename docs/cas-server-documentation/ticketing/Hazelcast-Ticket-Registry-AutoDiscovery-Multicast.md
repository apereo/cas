---
layout: default
title: CAS - Hazelcast Ticket Registry - Multicast Auto Discovery
category: Ticketing
---

{% include variables.html %}

# Hazelcast Ticket Registry - Multicast Auto Discovery

With the multicast auto-discovery mechanism, Hazelcast allows cluster members to find
each other using multicast communication. The cluster members do not need to know the
concrete addresses of the other members, as they just multicast to all the other
members for listening. Whether multicast is possible or allowed **depends on your environment**.

Pay special attention to timeouts when multicast is enabled. Multicast timeout specifies
the time in seconds that a member should wait for a valid multicast response from another
member running in the network before declaring itself the leader
member (the first member joined to the cluster) and creating its own cluster. This
only applies to the startup of members where no leader has been assigned yet. If
you specify a high value such as 60 seconds, it means that until a leader is selected
each member will wait 60 seconds before moving on. Be careful when providing a high
value. Also, be careful not to set the value too low, or the members might give
up too early and create their own cluster.

{% include_cached casproperties.html properties="cas.ticket.registry.hazelcast.cluster.discovery.multicast." %}
