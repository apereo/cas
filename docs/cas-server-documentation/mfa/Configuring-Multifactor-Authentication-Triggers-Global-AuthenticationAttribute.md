---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Groovy Authentication Attribute - Multifactor Authentication Triggers

MFA can be triggered for all users/subjects whose *authentication event/metadata* has resolved a specific attribute
that matches one of the below conditions:

- Trigger MFA based on a *authentication attribute(s)* whose value(s) matches a regex pattern.
**Note** that this behavior is only applicable if there is only a **single MFA provider** configured, since that would allow CAS
to know what provider to next activate.

- Trigger MFA based on a *authentication attribute(s)* whose value(s) **EXACTLY** matches an MFA provider.
This option is more relevant if you have more than one provider configured or if you have the flexibility of assigning
provider ids to attributes as values.

Needless to say, the attributes need to have been resolved for the authentication event prior to this step. This trigger
is generally useful when the underlying authentication engine signals CAS to perform additional validation of credentials.
This signal may be captured by CAS as an attribute that is part of the authentication event metadata which can then trigger
additional multifactor authentication events.

An example of this scenario would be the "Access Challenge response" produced by RADIUS servers.

{% include_cached casproperties.html properties="cas.authn.mfa.triggers.authentication" %}
