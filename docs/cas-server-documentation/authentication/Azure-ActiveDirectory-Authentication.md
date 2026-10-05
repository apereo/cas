---
layout: default
title: CAS - Microsoft Entra ID Authentication
category: Authentication
---
{% include variables.html %}


# Microsoft Entra ID Authentication

[Microsoft Entra ID](https://learn.microsoft.com/en-us/entra/fundamentals/what-is-entra) is Microsoft’s cloud-based identity and access management service. The functionality described here allows one to authenticate credentials using
Microsoft Entra ID as the account store and optionally fetch user attributes using Microsoft Graph.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-azuread-authentication" %}

{% include_cached casproperties.html properties="cas.authn.azure-active-directory" %}

## Principal Attributes

The above dependency may also be used, in the event that principal attributes 
need to be fetched from Microsoft Entra ID without necessarily authenticating credentials .

{% include_cached casproperties.html properties="cas.authn.attribute-repository.azure-active-directory" %}
