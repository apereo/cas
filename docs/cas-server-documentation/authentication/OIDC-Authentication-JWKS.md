---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# JWKS - OpenID Connect Authentication

The JWKS (JSON Web Key Set) endpoint and functionality returns a JWKS containing public keys that enable 
clients to validate a JSON Web Token (JWT) issued by CAS as an OpenID Connect Provider.

{% include_cached casproperties.html properties="cas.authn.oidc.jwks.core" %}

## Keystore Storage
       
Please [see this guide](OIDC-Authentication-JWKS-Storage.html) for more info.

## Key Rotation

Please [see this guide](OIDC-Authentication-JWKS-Rotation.html) for more info.

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="oidcJwks" casModule="cas-server-support-oidc" %}
