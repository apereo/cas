---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Passwordless Authentication - Token Management

The following strategies define how issued tokens may be managed by CAS. 

{% include_cached casproperties.html properties="cas.authn.passwordless.tokens" includes=".core,.crypto" %}

| Option  | Description                                                                                                     |
|---------|-----------------------------------------------------------------------------------------------------------------|
| Memory  | This is the default option where tokens are kept in memory using a cache with a configurable expiration period. |
| MongoDb | Please [see this guide](Passwordless-Authentication-Tokens-MongoDb.html).                                       |
| JPA     | Please [see this guide](Passwordless-Authentication-Tokens-JPA.html).                                           |
| REST    | Please [see this guide](Passwordless-Authentication-Tokens-Rest.html).                                          |
| Custom  | Please [see this guide](Passwordless-Authentication-Tokens-Custom.html).                                        |
