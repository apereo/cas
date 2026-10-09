---
layout: default
title: CAS - Microsoft Entra ID Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Microsoft Entra ID Attribute Resolution

The following configuration describes how to fetch and retrieve 
attributes from Microsoft Entra ID attribute repositories.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-azuread-authentication" %}

{% include_cached casproperties.html properties="cas.authn.attribute-repository.azure-active-directory" %}
