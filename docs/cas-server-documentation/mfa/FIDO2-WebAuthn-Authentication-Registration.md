---
layout: default
title: CAS - FIDO2 WebAuthn Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# FIDO2 WebAuthn Multifactor Authentication - Registration

Device registration flows are baked into CAS automatically. A background 
*cleaner* process is also automatically scheduled to scan the 
repository periodically and remove expired device registration records 
based on configured parameters. In the default setting, devices
expire after a fixed period since a user registered their device. 

{% include_cached casproperties.html properties="cas.authn.mfa.web-authn.cleaner" %}

<div class="alert alert-warning">:warning: <strong>Cleaner Usage</strong><p>In a clustered CAS deployment, it is best to keep 
the cleaner running on one designated CAS node only and turn it off on all others via CAS settings. Keeping the cleaner running 
on all nodes may likely lead to severe performance and locking issues.</p></div>

By default, a repository implementation is included that collects
user device registrations and saves them into memory.
This option should only be used for demo and testing purposes.

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="webAuthnDevices" %}

## Device Repositories

Device registrations can also be managed using any one of the following strategies.

| Storage  | Description                                                                 |
|----------|-----------------------------------------------------------------------------|
| JSON     | [See this guide](FIDO2-WebAuthn-Authentication-Registration-JSON.html).     |
| MongoDb  | [See this guide](FIDO2-WebAuthn-Authentication-Registration-MongoDb.html).  |
| LDAP     | [See this guide](FIDO2-WebAuthn-Authentication-Registration-LDAP.html).     |
| JPA      | [See this guide](FIDO2-WebAuthn-Authentication-Registration-JPA.html).      |
| Redis    | [See this guide](FIDO2-WebAuthn-Authentication-Registration-Redis.html).    |
| DynamoDb | [See this guide](FIDO2-WebAuthn-Authentication-Registration-DynamoDb.html). |
| REST     | [See this guide](FIDO2-WebAuthn-Authentication-Registration-Rest.html).     |


