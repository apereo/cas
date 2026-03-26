---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Verifiable Credentials - OpenID Connect Authentication

OpenID Connect Authentication can be used in conjunction with Verifiable Credentials to 
provide a secure and decentralized way of verifying user identities. Verifiable Credentials 
are digital credentials that can be issued by trusted authorities and can be presented by users 
to prove their identity or attributes.
                         
The following capabilities are in place:

- Issuer metadata is published via the `.well-known/openid-credential-issuer` endpoint.
- CAS may issue credentials formats such as `SD-JWT VC` (selective disclosure), etc.
- Dedicated endpoints for credential issuance are available.

## Overview

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-oidc-vc" %}

{% include_cached casproperties.html properties="cas.authn.oidc.vc" %}
