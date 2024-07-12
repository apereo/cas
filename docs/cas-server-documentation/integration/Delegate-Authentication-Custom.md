---
layout: default
title: CAS - Delegate Authentication
category: Authentication
---

{% include variables.html %}

# Custom

For an overview of the delegated authentication flow, please [see this guide](Delegate-Authentication.html).
          
Delegated or external identity providers can be customized in a variety of ways. You can customize and modify
the configuration of an existing identity provider built by CAS to change or override certain aspects of its behavior, 
or you could register an entirely new identity provider with CAS.

## Existing Identity Provider

To customize and modify the configuration of an existing identity provider, you will need to build and register
the following `@Bean` in your project:

```java
@Bean
public DelegatedClientFactoryCustomizer myCustomizer() {
    return client -> {
        if (client instanceof MyClient myClient) {
            // Customize here...
        }
    };
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.

## New Identity Provider

To register your own external identity provider with CAS, you will need to build and register
the following `@Bean` in your project:

```java
@Bean
public DelegatedIdentityProviderFactory pac4jDelegatedClientFactory() {
    return new MyDelegatedIdentityProviderFactory();
}
```

The `DelegatedIdentityProviderFactory` is responsible for (re)building the delegated identity provider instances. To learn
more about existing options that allow you to register identity providers with CAS, 
please [see this guide](../integration/Delegate-Authentication-Provider-Registration.html). Just as before,
[see this guide](../configuration/Configuration-Management-Extensions.html) to learn more about how to 
register configurations into the CAS runtime.

If you don't wish to build your own factory implementation, there is an easier option that allows you to
rely on the existing `DelegatedIdentityProviderFactory` and simply build and supply the identity provider instances:

```java
@Bean
public ConfigurableDelegatedClientBuilder myClientBuilder() {
    return new MyDelegatedClientBuilder();
}
```
         
You may define as many `ConfigurableDelegatedClientBuilder` instance you need. The `DelegatedIdentityProviderFactory` 
will then automatically pick up the new identity provider instances, configure and initialize them for use with CAS.
