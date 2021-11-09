---
layout: default
title: CAS - Hazelcast Ticket Registry - Azure Auto Discovery
category: Ticketing
---

{% include variables.html %}

# Hazelcast Ticket Registry - Azure Auto Discovery

Hazelcast support in CAS may handle auto-discovery automatically via Microsoft
Azure. The discovery strategy will provide all Hazelcast instances by returning
VMs within your Azure resource group that are tagged with a specified value. You
will need to setup [Azure Active Directory Service Principal credentials](https://azure.microsoft.com/en-us/documentation/articles/resource-group-create-service-principal-portal/) for
your Azure Subscription for this plugin to work. With every Hazelcast Virtual Machine
you deploy in your resource group, you need to ensure that each VM is tagged with the
value of `clusterId` defined in the CAS Hazelcast configuration. The only requirement
is that every VM can access each other either by private or public IP address.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-hazelcast-discovery-azure" %}

{% include_cached casproperties.html properties="cas.ticket.registry.hazelcast.cluster.discovery.azure" %}
