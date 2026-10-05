---
layout: default
title: CAS - OAuth20 - Delegated Authentication
category: Authentication
---

{% include variables.html %}

# OAuth20

Let users log in through any OAuth 2.0 provider that has no dedicated integration, by describing its endpoints and profile attributes in CAS settings.

For an overview of the delegated authentication flow, please [see this guide](Delegate-Authentication.html).

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pac4j-oidc" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.pac4j.oauth2" %}
