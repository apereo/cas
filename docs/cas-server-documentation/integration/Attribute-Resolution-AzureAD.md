---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Microsoft Azure Active Directory Attribute Resolution

The following configuration describes how to fetch and retrieve 
attributes from Microsoft Azure Active Directory attribute repositories.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-azuread-authentication" %}

{% include_cached casproperties.html properties="cas.authn.attribute-repository.azure-active-directory" %}
