---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}

# Surrogate Authentication

Surrogate authentication (impersonation), sometimes known as *sudo for the web*, 
is the ability to authenticate on behalf of another user. 

The two actors in this case are:

1. The primary admin user whose credentials are verified upon authentication.
2. The surrogate user, selected by the admin, to which CAS will switch after credential verification and is one that is linked to the single sign-on session.

Example use cases for impersonation include:

1. Logging into an application on behalf of a user to execute and make changes.
2. Troubleshoot a bothersome authentication experience with an application on behalf of another user.

Surrogate authentication is enabled by including the following dependencies in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-surrogate-webflow" %}
    
## Configuration

{% include_cached casproperties.html properties="cas.authn.surrogate.core" %}

## Account Storage

The following account stores may be configured and used to locate surrogates authorized for a particular user.

| Storage | Description                                                     |
|---------|-----------------------------------------------------------------|
| Simple  | [See this guide](Surrogate-Authentication-Storage-Simple.html). |
| JSON    | [See this guide](Surrogate-Authentication-Storage-JSON.html).   |
| LDAP    | [See this guide](Surrogate-Authentication-Storage-LDAP.html).   |
| JDBC    | [See this guide](Surrogate-Authentication-Storage-JDBC.html).   |
| REST    | [See this guide](Surrogate-Authentication-Storage-REST.html).   |
| Groovy  | [See this guide](Surrogate-Authentication-Storage-Groovy.html). |
| Custom  | [See this guide](Surrogate-Authentication-Storage-Custom.html). |
     
Note that multiple account stores may be combined and can function simultaneously together to locate
accounts from different stores.

## Account Selection

Please see [this guide](Surrogate-Authentication-AccountSelection.html).

## Session Expiration

Please see [this guide](Surrogate-Authentication-Session-Expiration.html).

## Surrogate Attributes

Upon a successful surrogate authentication event, the following 
attributes are communicated back to the application in order to detect an impersonation session:

| Attribute            | Instructions                                                                 |
|----------------------|------------------------------------------------------------------------------|
| `surrogateEnabled`   | Boolean to indicate whether session is impersonated.                         |
| `surrogatePrincipal` | The admin user whose credentials are validated and acts as the impersonator. |
| `surrogateUser`      | The surrogate user that is impersonated.                                     |

## Surrogate Access Strategy

Please see [this guide](Surrogate-Authentication-AccessStrategy.html).             

## Surrogate Audits

Please see [this guide](Surrogate-Authentication-Audit.html).
 
## Surrogate Principal Resolution

Please see [this guide](Surrogate-Authentication-Principal-Resolution.html).

## REST Protocol

The feature extends the [CAS REST API](../protocol/REST-Protocol.html) communication model to surrogate authentication,
allowing REST credentials to specify a substitute and authenticate on behalf of another user. To activate surrogate authentication
for the CAS REST API, you will need to choose one of the following options:

- Format the credential username using the following syntax:

```bash
[surrogate-userid][separator][primary-userid]
```

- Pass along a special request header `X-Surrogate-Principal` that contains the surrogate userid.
