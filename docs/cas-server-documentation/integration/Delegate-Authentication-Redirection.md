---
layout: default
title: CAS - Delegate Authentication Redirection
category: Authentication
---

{% include variables.html %}

# Delegated Authentication - Auto Redirection/Selection

By default, the list of available identity providers are displayed in a selection menu and the user is allowed to 
choose the provider. In certain scenarios, the browser may be instructed to auto-redirect to a pre-selected identity provider.
  
## Pre-selected Identity Provider

An identity provider can be instructed via CAS configuration to always perform an auto-redirect, regardless of the application
type and/or authentication requests. The selected identity provider is considered by CAS to be the *primary* strategy for handling
authentication requests.

## Identity Provider Exclusivity
    
Authentication requests from the following application will be auto-redirected to the identity provider that is identified 
as `Twitter` in the CAS configuration, since the delegated authentication policy only allows the single exclusive use of this provider, removing selection menu and the ability to choose other alternative authentication methods.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "delegatedAuthenticationPolicy" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy",
      "allowedProviders" : [ "java.util.ArrayList", [ "Twitter" ] ],
      "exclusive": true
    }
  }
}
```

## Identity Provider Cookie Selection

A chosen identity provider from the selection menu can be optionally tracked and stored using a dedicated cookie,
which will then be used on subsequent attempts to auto-redirect to the identity provider, skipping the selection menu.

{% include_cached casproperties.html properties="cas.authn.pac4j.cookie" %}

## Identity Provider Groovy Selection

The auto-redirection strategy of a given identity provider may also be decided dynamically via a Groovy resource 
whose path is defined via CAS settings. 

{% include_cached casproperties.html properties="cas.authn.pac4j.core.groovy-redirection-strategy" %}

The Groovy script would have the following outline:

```groovy
import org.apereo.cas.web.*
import org.pac4j.core.context.*
import org.apereo.cas.pac4j.*
import org.apereo.cas.web.support.*
import java.util.stream.*
import java.util.*
import org.apereo.cas.configuration.model.support.delegation.*

def run(Object[] args) {
    def requestContext = args[0]
    def service = args[1]
    def registeredService = args[2]
    def providers = args[3] as Set<DelegatedClientIdentityProviderConfiguration>
    def applicationContext = args[4]
    def logger = args[5]

    providers.forEach(provider -> {
        logger.info("Checking ${provider.name}...")
        if (provider.name.equals("Twitter")) {
            provider.autoRedirectType = DelegationAutoRedirectTypes.CLIENT
            return provider
        }
    })
    return null
}
```

The following parameters are passed to the script:

| Parameter             | Description
|---------------------------------------------------------------------------------------------------------
| `requestContext`        | Reference to the Spring Webflow request context, as `RequestContext`.
| `service`               | Reference to the application authentication request as `Service`, if any.
| `registeredService`     | Reference to registered service definition, if any.
| `providers`              | Reference to the set of identity provider configuration identified as `DelegatedClientIdentityProviderConfiguration`.
| `applicationContext`    | Reference to the application context as `ApplicationContext`.
| `logger`                | The object responsible for issuing log messages such as `logger.info(...)`.

## Identity Provider Selection Per Service

The auto-redirection strategy of a given identity provider may also be decided dynamically via a Groovy resource
whose path specified directly in the service definition as part of the authentication policy's provider selection strategy:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "delegatedAuthenticationPolicy" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy",
      "selectionStrategy": "file:/path/to/script.groovy"
    }
  }
}
```

<div class="alert alert-info mt-3"><strong>Usage</strong><p>
If you wish, you may also use a <i>Groovy Inline</i> syntax using the <code>groovy {...}</code> construct.</p></div>

The collection of parameters and the script body are identical to the *Identity Provider Groovy Selection* option above.

## Identity Provider Custom Selection

If you wish to create your own redirection strategy, you will need to
design a component and register it with CAS as such:

```java
@Bean
public DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy() {
    return new CustomDelegatedClientIdentityProviderRedirectionStrategy(); 
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
