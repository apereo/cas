---
layout: default
title: CAS - Delegate Authentication
category: Authentication
---

{% include variables.html %}

# Google OpenID Connect

For an overview of the delegated authentication flow, please [see this guide](Delegate-Authentication.html).

{% include {{ version }}/delegated-authentication-configuration.md configKey="cas.authn.pac4j.oidc[0].google" %}
{% include {{ version }}/oidc-delegated-authentication-configuration.md configKey="cas.authn.pac4j.oidc[0].google" %}
