---
layout: default
title: CAS - Wordpress - Delegated Authentication
category: Authentication
---

{% include variables.html %}

# Wordpress

Let users log in to CAS with their WordPress.com account.

For an overview of the delegated authentication flow, please [see this guide](Delegate-Authentication.html).

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pac4j-oidc" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.pac4j.wordpress" %}
