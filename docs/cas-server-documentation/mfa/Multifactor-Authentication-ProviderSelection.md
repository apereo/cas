---
layout: default
title: CAS - Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Multifactor Authentication - Provider Selection

In the event that multiple multifactor authentication providers are determined for a
multifactor authentication transaction, CAS presents options to allow for a selection to be made manually or dynamically.
The selection strategies involve choosing a multifactor authentication provider via rankings, in a scripted fashion, or 
allowing the user to see a selection menu.

## Configuration

{% include_cached casproperties.html properties="cas.authn.mfa.core" %}

## Ranking Providers

At times, CAS needs to determine the correct provider when step-up authentication is required. Consider for a moment that CAS
already has established an SSO session with/without a provider and has reached a level of authentication. Another incoming
request attempts to exercise that SSO session with a different and often competing authentication requirement that may differ
from the authentication level CAS has already established. Concretely, examples may be:

- CAS has achieved an SSO session, but a separate request now requires step-up authentication with DuoSecurity.
- CAS has achieved an SSO session with an authentication level satisfied by DuoSecurity, but a separate request now requires step-up authentication with YubiKey.

By default, CAS will attempt to rank authentication levels and compare them with each other. If CAS already has achieved a level
that is higher than what the incoming request requires, no step-up authentication will be performed. If the opposite is true, CAS will
route the authentication flow to the required authentication level and upon success, will adjust the SSO session with the new higher
authentication level now satisfied.

Ranking of authentication methods is done per provider via specific properties for each in CAS settings. Note that
the higher the rank value is, the higher on the security scale it remains. A provider that ranks higher with a larger weight value trumps
and override others with a lower value.

## Groovy Selection

In the event that multiple multifactor authentication providers are determined for a 
multifactor authentication transaction, by default CAS will attempt to sort the 
collection of providers based on their rank and will pick one with the highest 
priority. This use case may arise if multiple triggers are defined where each 
decides on a different multifactor authentication provider, or the same 
provider instance is configured multiple times with many instances.

Provider selection may also be carried out using Groovy scripting strategies more dynamically. 

The following example should serve as an outline of how to select multifactor providers based on a Groovy script:

```groovy
import java.util.*

class SampleGroovyProviderSelection {
    def run(final Object... args) {
        def (service,principal,providersCollection,logger) = args
        ...
        return "mfa-duo"
    }
}
```

The parameters passed are as follows:

| Parameter             | Description                                                                                              |
|-----------------------|----------------------------------------------------------------------------------------------------------|
| `service`             | The object representing the incoming service provided in the request, if any.                            |
| `principal`           | The object representing the authenticated principal along with its attributes.                           |
| `providersCollection` | The object representing the collection of candidate multifactor providers qualified for the transaction. |
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`.                              |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

## User Selection Menu

When multifactor provider selection is enabled via CAS settings, the user will be presented with a list of providers
that are candidates to carry out the multifactor authentication request. Enabling the selection menu of course only
makes sense if there are in fact multiple multifactor authentication providers available and configured in CAS.

This capability also presents the option to make multifactor authentication *optional*, where the user may choose
to skip the multifactor authentication step altogether. This behavior needs to be explicitly enabled in CAS settings.
