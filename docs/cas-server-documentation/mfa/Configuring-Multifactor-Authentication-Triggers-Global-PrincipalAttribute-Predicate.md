---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Groovy Principal Attribute Predicate - Multifactor Authentication Triggers

This is a more generic variant of the above trigger. It may be useful in cases where there is more than one provider configured and available in the application runtime and you need to design a strategy to dynamically decide on the provider that should be activated for the request.

The decision is handed off to a `Predicate` implementation defined in a Groovy script whose location is taught to CAS. The responsibility of the `test` function in the script is to determine eligibility of the provider to be triggered. If the predicate determines multiple providers as eligible by returning `true` more than one, the first provider in the sorted result set ranked by the provider's order will be chosen to respond.

The Groovy script predicate may be designed as such:

```groovy
import org.apereo.cas.authentication.*
import java.util.function.*
import org.apereo.cas.services.*

class PredicateExample implements Predicate<MultifactorAuthenticationProvider> {

    def service
    def principal
    def providers
    def logger

    public PredicateExample(service, principal, providers, logger) {
        this.service = service
        this.principal = principal
        this.providers = providers
        this.logger = logger
    }

    @Override
    boolean test(final MultifactorAuthenticationProvider p) {
        ...
    }
}
```

The parameters passed are as follows:

| Parameter   | Description                                                                              |
|-------------|------------------------------------------------------------------------------------------|
| `service`   | The object representing the corresponding service definition in the registry.            |
| `principal` | The object representing the authenticated principal.                                     |
| `providers` | Collection of `MultifactorAuthenticationProvider`s from which a selection shall be made. |
| `logger`    | The object responsible for issuing log messages such as `logger.info(...)`.              |

{% include_cached casproperties.html properties="cas.authn.mfa.triggers.principal" %}

As an example, the following predicate example will begin to test each multifactor 
authentication provider and if the given provider is `mfa-duo` it will accept it 
as a valid trigger so long as the provider can be reached.

```groovy
import org.apereo.cas.authentication.*
import java.util.function.*
import org.apereo.cas.services.*

class PredicateExample implements Predicate<MultifactorAuthenticationProvider> {

    def service
    def principal
    def providers
    def logger

    public PredicateExample(service, principal, providers, logger) {
        this.service = service
        this.principal = principal
        this.providers = providers
        this.logger = logger
    }

    @Override
    boolean test(final MultifactorAuthenticationProvider p) {
        logger.info("Testing provider {}", p.getId())
        if (p.matches("mfa-duo")) {
           logger.info("Provider {} is available. Checking eligibility...", p.getId())
           if (p.isAvailable(this.service)) {
               logger.info("Provider {} matched. Good to go!", p.getId())
               return true;
           }
           logger.info("Skipping provider {}. Match failed.", p.getId())
           return false; 
        }
        logger.info("Provider {} cannot be reached", p.getId())
        return false
    }
}
```
