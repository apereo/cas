---
layout: default
title: CAS - Authy Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Authy Authentication

CAS provides support for Authy's [TOTP API](https://www.twilio.com/docs/authy/api). This is done
via Authy's REST API that does all the heavy lifting.

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-authy" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.mfa.authy" %}

## Registration

By default, users are registered with authy based on their phone and email attributes retrieved by CAS.
