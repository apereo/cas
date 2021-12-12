---
layout: default
title: CAS - Delegate Authentication
category: Authentication
---

{% include variables.html %}

# Delegated Authentication Provisioning

By default, user profiles that are extracted from external identity providers and merged into a CAS
authenticated principal are not stored or tracked anywhere. CAS does provide additional options to allow
such profiles to be managed outside of CAS and/or provisioned into identity stores, allowing you optionally to link
external/guest accounts with their equivalent found in the authentication source used by CAS, etc.

## Groovy Provisioner
   
{% include_cached casproperties.html properties="cas.authn.pac4j.provisioning.groovy" %}

Provisioning tasks can be carried out using an external Groovy script with the following structure:

```groovy
def run(Object[] args) {
    def principal = args[0]
    def userProfile = args[1]
    def client = args[2]
    def logger = args[3]
    ...
}
```

It is not expected for the script to return a value. The following parameters are passed to the script:

| Parameter     | Description                                                                                    |
|---------------|------------------------------------------------------------------------------------------------|
| `principal`   | CAS authenticated `Principal` that contains all attributes and claims.                         |
| `userProfile` | The original `UserProfile` extracted from the external identity provider.                      |
| `client`      | The `Client` configuration responsible for the exchange between CAS and the identity provider. |
| `logger`      | The object responsible for issuing log messages such as `logger.info(...)`.                    |

## REST Provisioner

{% include_cached casproperties.html properties="cas.authn.pac4j.provisioning.rest" %}

Provisioning tasks can be carried out using an external REST endpoint expected to receive the following:
     
| Header                | Description                                                                         |
|-----------------------|-------------------------------------------------------------------------------------|
| `principalId`         | CAS authenticated principal identifier.                                             |
| `principalAttributes` | CAS authenticated principal attributes.                                             |
| `profileId`           | The identifier of the user profile extracted from the identity provider.            |
| `profileTypedId`      | The *typed* identifier of the user profile extracted from the identity provider.    |
| `profileAttributes`   | Collection of attributes extracted from the identity provider's response.           |
| `clientName`          | The client name responsible for the exchange between CAS and the identity provider. |
 
## SCIM Provisioner

{% include_cached casproperties.html properties="cas.authn.pac4j.provisioning.scim" %}

Provisioning tasks can be carried out using the CAS [SCIM integration](../integration/SCIM-Integration.html). 
Once enabled and configured, authenticated profiles established from the external identity provider may be
provisioned via SCIM to other systems.

<div class="alert alert-info"><strong>Usage</strong><p>SCIM integration support for 
delegated authentication is only handled via SCIM <code>v2</code>.</p></div>

## Custom Provisioner

If you wish to create your own provisioner for delegated authentication, you will need to
design a component and register it with CAS as such:

```java
@Bean
public DelegatedClientUserProfileProvisioner clientUserProfileProvisioner() {
    return new CustomDelegatedClientUserProfileProvisioner();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
