---
layout: default
title: CAS - Delegate Authentication
category: Authentication
---

{% include variables.html %}

# OpenID Connect Generic

For an overview of the delegated authentication flow, please [see this guide](Delegate-Authentication.html).

{% include {{ version }}/delegated-authentication-configuration.md configKey="cas.authn.pac4j.oidc[0].generic" %}
{% include {{ version }}/oidc-delegated-authentication-configuration.md configKey="cas.authn.pac4j.oidc[0].generic" %}
