---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Default Bundle

CAS provides the ability to release a bundle of principal attributes to all services by default. This bundle
is not defined on a per-service basis and is always combined with attributes produced by the specific
release policy of the service, such that for instance, you can devise rules to always release `givenName`
and `cn` to every application, and additionally allow other specific principal attributes for
only some applications per their attribute release policy.

{% include_cached casproperties.html properties="cas.authn.attribute-repository.core.default-attributes-to-release" %}
