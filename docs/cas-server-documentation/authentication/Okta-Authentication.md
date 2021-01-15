---
layout: default
title: CAS - Okta Authentication
category: Authentication
---
{% include variables.html %}


# Okta Authentication

The integration with Okta is a convenience wrapper around [Okta's Authentication API](https://developer.okta.com/docs/api/resources/authn.html) and 
is useful if you need to accept and validate credentials managed by Okta.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-okta-authentication" %}

{% include casproperties.html properties="cas.authn.okta"  %}
