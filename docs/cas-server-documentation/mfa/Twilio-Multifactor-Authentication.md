---
layout: default
title: CAS - Twilio Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Twilio Multifactor Authentication
         
Multifactor authentication via [Twilio](https://www.twilio.com/) allows CAS to use the Twilio verification service
and APIs to send passcodes to end-users via SMS, WhatsApp or voice calls, etc and verify the attempt subsequently
by asking the user to provide the code back.

The recipient is determined as a principal attribute configured in CAS settings. Multiple verification channels are supported.

## Configuration

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-twilio-mfa" %}

{% include_cached casproperties.html properties="cas.authn.mfa.twilio" %}

### Bypass

{% include_cached casproperties.html properties="cas.authn.mfa.twilio" includes=".bypass" %}
