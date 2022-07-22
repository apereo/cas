---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}

# Token Expiration Policy - OpenID Connect Authentication

The expiration policy for OpenID Connect tokens is controlled by CAS settings and properties. These settings
generally are the same as those that are defined for the OAuth Protocol. 
[See this guide](OAuth-Authentication-TokenExpirationPolicy.html) for more info.
      
## ID Tokens

{% include_cached casproperties.html properties="cas.authn.oidc.id-token" %}
