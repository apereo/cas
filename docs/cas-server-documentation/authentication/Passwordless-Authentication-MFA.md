---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Passwordless Authentication - Multifactor Authentication

Passwordless authentication can be integrated 
with [CAS multifactor authentication providers](../mfa/Configuring-Multifactor-Authentication.html). In this scenario,
once CAS configuration is enabled to support this behavior via settings 
or the located passwordless user account is considered *eligible* for multifactor authentication,
CAS will allow passwordless authentication to skip its 
own *intended normal* flow (i.e. as described above with token generation, etc) in favor of 
multifactor authentication providers that may be available and defined in CAS.

This means that if [multifactor authentication providers](../mfa/Configuring-Multifactor-Authentication.html) are 
defined and activated, and defined 
[multifactor triggers](../mfa/Configuring-Multifactor-Authentication-Triggers.html) in CAS 
signal availability and eligibility of an multifactor flow for the given passwordless user, CAS will skip 
its normal passwordless authentication flow in favor of the requested multifactor 
authentication provider and its flow. If no multifactor providers 
are available, or if no triggers require the use of multifactor authentication 
for the verified passwordless user, passwordless authentication flow will commence as usual.

