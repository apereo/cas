---
layout: default
title: CAS - Delegate Authentication Discovery Selection
category: Authentication
---

{% include variables.html %}

# Delegated Authentication Discovery Selection

External identity providers available to CAS are presented to the user and available for discovery and selection
using the following strategies.

<div class="alert alert-info mt-3"><strong>Remember</strong><p>
If you are interested in supporting discovery of SAML2 identity providers using a discovery service,
you may want to take a look at <a href="Delegate-Authentication-SAML.html">this guide</a>.
</p></div>

{% include_cached casproperties.html properties="cas.authn.pac4j.core.discovery-selection" %}

## Menu Selection
     
This is the default and most common strategy that allows external identity providers to be listed on the login page, 
allowing the user to *order one from the menu* and select one from the list of options.

## Dynamic Selection
   
Rather than listing all available identity providers, this option allows CAS to auto-select the appropriate
identity provider in a dynamic fashion using pre-defined rules and conditions and 
based on the user identifiers such as username, email address, etc. For example, once the user providers 
their identifier, i.e. `casuser@example.org`, the discovery strategy can try to select the correct identity provider
based on the email domain.

### JSON Rules

By default, the selection rules and conditions can be specified in a JSON file with the following structure:

```json
{
    "@class" : "java.util.HashMap",
    "<key-pattern>" : {
      "@class" : "org.apereo.cas.pac4j.discovery.DelegatedAuthenticationDynamicDiscoveryProvider",
      "clientName" : "SAML2Client",
      "order": 0
    }
}
```

The following parameters are available to the JSON resource:

| Parameter       | Description                                                                                                   |
|-----------------|---------------------------------------------------------------------------------------------------------------|
| `<key-pattern>` | Regular expression pattern matched against the user identifier to locate the provider. i.e. `.+@example.org`. |
| `clientName`    | The client name that should be used for this match, found and defined in CAS configuration.                   |
| `order`         | The selection sorting order, used to properly sequence and prioritize entries in case there is overlap.       |

## Custom

If you wish to create your own strategy to dynamically locate identity providers for 
delegated authentication discovery, you will need to design a component and register it with CAS as such:

```java
@Bean
public DelegatedAuthenticationDynamicDiscoveryProviderLocator delegatedAuthenticationDynamicDiscoveryProviderLocator() {
    return new CustomDelegatedAuthenticationDynamicDiscoveryProviderLocator();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
