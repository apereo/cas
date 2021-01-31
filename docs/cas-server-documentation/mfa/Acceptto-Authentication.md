---
layout: default
title: CAS - Acceptto Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Acceptto Authentication

Secure your workforce identity with [Acceptto](https://www.acceptto.com) 
end-to-end risk-based multiFactor authentication.

Start by visiting the [Acceptto documentation](https://www.acceptto.com/acceptto-mfa-rest-api/).

Support is enabled by including the following module in the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-acceptto-mfa" %}

The integration adds support for both multifactor authentication and QR passwordless authentication.

## Integration with DBFP

The integration is able to handle the integration with DBFP and will set a cookie 
named `jwt` that is passed to the Acceptto API. This parameter contains a value that the server uses 
to assess the risk of authentication request including browser fingerprint, IP address of user and 
GPS location of the user’s browser. The server compares this data with the 
history of user behavior data to detect anomalies.

## Configuration

{% include casproperties.html properties="cas.authn.mfa.acceptto,cas.session-replication" %}

