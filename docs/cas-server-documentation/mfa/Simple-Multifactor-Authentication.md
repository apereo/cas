---
layout: default
title: CAS - Simple Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Simple Multifactor Authentication

Allow CAS to act as a multifactor authentication provider on its own, issuing tokens 
and sending them to end-users via pre-defined communication channels such as email or text messages.

## Configuration

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-simple-mfa" %}

{% include_cached casproperties.html properties="cas.authn.mfa.simple" excludes=".token,.mail,.sms,.bucket4j" %}

## Registration

Registration is expected to have occurred as an out-of-band process. Ultimately,
CAS expects to fetch the necessary attributes from configured attribute sources to determine communications channels for
email and/or sms. The adopter is expected to have populated user records with enough information to indicate a phone number and/or email
address where CAS could then be configured to fetch and examine those attributes to share generated tokens.

## Communication Strategy

Please see [this guide](Simple-Multifactor-Authentication-Communication.html).

## Rate Limiting

Please see [this guide](Simple-Multifactor-Authentication-RateLimiting.html).

## Token Management

Please see [this guide](Simple-Multifactor-Authentication-TokenManagement.html).

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="mfaSimple" %}

## REST Protocol Credential Extraction

In the event that the [CAS REST Protocol](../protocol/REST-Protocol.html) is turned on,
a special credential extractor is injected into the REST authentication engine in
order to recognize simple multifactor credentials and authenticate them as part of the REST
request. The expected parameter name in the request body is `sotp`.
