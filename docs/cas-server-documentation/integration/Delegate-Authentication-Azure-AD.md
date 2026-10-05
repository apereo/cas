---
layout: default
title: CAS - Microsoft Entra ID - Delegated Authentication
category: Authentication
---

{% include variables.html %}

# Microsoft Entra ID

Let users log in to CAS with their Microsoft Entra ID (formerly Azure AD) account through OpenID Connect.

For an overview of the delegated authentication flow, please [see this guide](Delegate-Authentication.html).

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pac4j-oidc" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.pac4j.oidc[].azure" %}
