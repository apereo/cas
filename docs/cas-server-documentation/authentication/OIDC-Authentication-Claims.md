---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Claims - OpenID Connect Authentication

OpenID connect claims are treated as normal CAS attributes that need to
be [resolved, mapped and released](../integration/Attribute-Release-Policies.html).

{% include_cached casproperties.html properties="cas.authn.oidc.core" %}

## Scope-based Claims

Please see [this guide](OIDC-Authentication-Claims-ScopeBased.html).

## Mapping Claims

Please see [this guide](OIDC-Authentication-Claims-Mapping.html).

## Releasing Claims

Please see [this guide](OIDC-Authentication-Claims-Release.html).

## Pairwise Identifiers

Please see [this guide](OIDC-Authentication-Claims-Pairwise.html).

## Subject Identifier Claim

Please see [this guide](OIDC-Authentication-Claims-Sub.html).
