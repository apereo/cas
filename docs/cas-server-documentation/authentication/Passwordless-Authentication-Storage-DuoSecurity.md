---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Duo Security - Passwordless Authentication Storage

[Duo Security](../mfa/DuoSecurity-Authentication.html) can also act as a passwordless account store. This behavior needs to be
explicitly turned on in CAS settings for eligible multifactor authentication providers. Once enabled, user accounts
that are found and registered with Duo Security with a valid email address and phone number will receive a push notification
from Duo Security and are able to login to CAS without the need for a password. Duo Security accounts that do not qualify 
will continue on with the normal CAS passwordless authentication flow.
