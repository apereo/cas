---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Groovy Principal Attribute - Multifactor Authentication Triggers

MFA can be triggered for all users/subjects carrying a specific 
attribute that matches one of the conditions below.

- Trigger MFA based on a principal attribute(s) whose value(s) matches a regex pattern.
**Note** that this behavior is only applicable if there is only a **single MFA provider** configured, since that would allow CAS
to know what provider to next activate.

- Trigger MFA based on a principal attribute(s) whose value(s) **EXACTLY** matches an MFA provider.
This option is more relevant if you have more than one provider configured or if you have the flexibility of assigning provider ids to attributes as values.

Needless to say, the attributes need to have been resolved for the principal prior to this step.

{% include casproperties.html properties="cas.authn.mfa.triggers.principal" %}


