---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# OpenID Connect Authentication JWKS Storage - JPA

Keystore generation can be outsourced to an external relational database, such as MySQL, etc.

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-oidc-jpa" %}

To learn how to configure database drivers and JPA implementation options, please [review this guide](../installation/JDBC-Drivers.html).

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.jpa" %}
