---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Okta Attribute Resolution

The following configuration describes how to fetch and retrieve attributes from [Okta instances](https://www.okta.com/).

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-okta-authentication" %}

{% include_cached casproperties.html properties="cas.authn.attribute-repository.okta" %}

