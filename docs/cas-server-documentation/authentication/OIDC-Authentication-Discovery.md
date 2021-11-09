---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Discovery - OpenID Connect Authentication

OpenID connect discovery is the process of determining the location of the provider. Discovery returns a 
JSON listing of the OpenID/OAuth endpoints, supported scopes and claims, 
public keys used to sign the tokens, and other details. The clients can use 
this information to construct a request to the CAS OpenID connect server.

{% include_cached casproperties.html properties="cas.authn.oidc.discovery" %}
