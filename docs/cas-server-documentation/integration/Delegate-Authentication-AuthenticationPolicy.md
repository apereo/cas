---
layout: default
title: CAS - Delegate Authentication Authentication Policy
category: Authentication
---

{% include variables.html %}

# Delegated Authentication - Authentication Policy

Service definitions may be conditionally authorized to use an external identity provider
by defining their own access strategy and authentication policy:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "delegatedAuthenticationPolicy" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy",
      "allowedProviders" : [ "java.util.ArrayList", [ "Facebook", "Twitter" ] ],
      "permitUndefined": true,
      "exclusive": false
    }
  }
}
```
     
The following fields are supported for the authentication policy:

| Type               | Description                                                                                                                                |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `allowedProviders` | The list of allowed providers should contain the external identity provider names (i.e. client names).                                     |
| `permitUndefined`  | Decides whether access should be granted in the event that no allowed providers are defined explicitly.                                    |
| `exclusive`        | Decides whether authentication should be exclusively limited to allowed providers, disabling other methods such as username/password, etc. |

## Configuration

{% include_cached casproperties.html properties="cas.authn.pac4j.core" %}

## Auto Redirection

By default, the list of available identity providers are displayed in a selection menu and the user is allowed to 
choose the provider. In certain scenarios, the browser may be instructed to auto-redirect to a pre-selected identity provider.
  
### Pre-selected Identity Provider

An identity provider can be instructed via CAS configuration to always perform an auto-redirect, regardless of the application
type and/or authentication requests. The selected identity provider is considered by CAS to be the *primary* strategy for handling
authentication requests.

### Identity Provider Exclusivity
    
Authentication requests from the following application will be auto-redirected to the identity provider that is identified 
as `Twitter` in the CAS configuration, since the delegated authentication policy only allows the single exclusive use of this provider,
removing selection menu and the ability to choose other alternative authentication methods.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
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

### Identity Provider Cookie Selection

A chosen identity provider from the selection menu can be optionally tracked and stored using a dedicated cookie,
which will then be used on subsequent attempts to auto-redirect to the identity provider, skipping the selection menu.

{% include_cached casproperties.html properties="cas.authn.pac4j.cookie" %}

### Identity Provider Groovy Selection

The auto-redirection strategy of a given identity provider may also be decided dynamically via a Groovy resource 
whose path is defined via CAS settings. The Groovy script would have the following outline:

```groovy
import org.apereo.cas.web.*

def run(Object[] args) {
    def requestContext = args[0]
    def service = args[1]
    def registeredService = args[2]
    def provider = args[3] as DelegatedClientIdentityProviderConfiguration
    def logger = args[4]
    logger.info("Checking ${provider.name}...")
    
    if (provider.name.equals("Twitter")) {
        provider.autoRedirectType = DelegationAutoRedirectTypes.CLIENT
        return provider
    }
    return null
}
```

The following parameters are passed to the script:

| Parameter             | Description
|---------------------------------------------------------------------------------------------------------
| `requestContext`        | Reference to the Spring Webflow request context, as `RequestContext`.
| `service`               | Reference to the application authentication request as `Service`, if any.
| `registeredService`     | Reference to registered service definition, if any.
| `provider`              | Reference to the identity provider configuration identified as `DelegatedClientIdentityProviderConfiguration`.
| `logger`                | The object responsible for issuing log messages such as `logger.info(...)`.

### Identity Provider Custom Selection

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
