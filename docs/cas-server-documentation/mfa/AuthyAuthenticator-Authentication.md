---
layout: default
title: CAS - Authy Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Authy Authentication

CAS provides support for Authy's [TOTP API](http://docs.authy.com/totp.html). This is done
via Authy's REST API that does all the heavy lifting.

Start by visiting the [Authy documentation](https://www.authy.com/developers/).

Support is enabled by including the following module in the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-authy" %}

## Configuration

{% include casproperties.html properties="cas.authn.mfa.authy" %}

## Registration

By default, users are registered with authy based on their phone and email attributes retrieved by CAS.
