---
layout: default
title: CAS - X (Twitter) - Delegated Authentication
category: Authentication
---

{% include variables.html %}

# X (Twitter)

Delegate authentication to X, the service formerly known as Twitter. The provider is still named `Twitter`
in CAS settings and client names.

For an overview of the delegated authentication flow, please [see this guide](Delegate-Authentication.html).

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pac4j-oidc" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.pac4j.twitter" %}
