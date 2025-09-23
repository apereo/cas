---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# LDAP Attribute Resolution

CAS does allow for attributes to be retrieved from LDAP or Active Directory.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-ldap" %}

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>
The inclusion of this module is only necessary if you are not already using LDAP authentication.</p></div>

The following configuration describes how to fetch and retrieve attributes from LDAP attribute repositories.

{% include_cached casproperties.html properties="cas.authn.attribute-repository.ldap" %}


## Multitenancy

Configuration settings for LDAP attribute resolution can be specified in a multitenant environment.
Please [review this guide](../multitenancy/Multitenancy-Overview.html) for more information.
