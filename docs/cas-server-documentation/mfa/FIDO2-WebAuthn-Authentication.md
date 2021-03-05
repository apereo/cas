---
layout: default
title: CAS - FIDO2 WebAuthn Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# FIDO2 WebAuthn Multifactor Authentication

[WebAuthn](https://webauthn.io/) is an API that makes it very easy 
for a relying party, such as a web service, to integrate strong 
authentication into applications using support built in to all leading browsers and platforms. This means 
that web services can now easily offer their users strong authentication with a choice of authenticators 
such as security keys or built-in platform authenticators such as biometric readers.

Support is enabled by including the following module in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-webauthn" %}

{% include casproperties.html properties="cas.authn.mfa.web-authn" %}

## Primary Authentication

It is possible to allow WebAuthN to act as a standalone authentication strategy for primary authentication. Using this approach,
user accounts and FIDO2-enabled devices that have already registered with 
CAS are given the option to login using their FIDO2-enabled
device by only providing the username linked to their registration record for a passwordless authentication experience.

Device registration can occur out of band using available CAS APIs, or by allowing users to pass through the registration flow
as part of the typical multifactor authentication. See below for details on device registration.


## Registration

Device registration flows are baked into CAS automatically. A background 
*cleaner* process is also automatically scheduled to scan the 
repository periodically and remove expired device registration records 
based on configured parameters. In the default setting, devices
expire after a fixed period since a user registered their device. If you deploy U2F
MFA for a setup where tokens are centrally distributed and revoked, 
you may want to extend the internal.

{% include casproperties.html properties="cas.authn.mfa.web-authn.cleaner" %}

<div class="alert alert-warning"><strong>Cleaner Usage</strong><p>In a clustered CAS deployment, it is best to keep 
the cleaner running on one designated CAS node only and turn it off on all others via CAS settings. Keeping the cleaner running 
on all nodes may likely lead to severe performance and locking issues.</p></div>

### Administrative Endpoints

The following endpoints are provided by CAS:
 
| Endpoint                                      | Description
|-----------------------------------------------|----------------------------------------------------------------------------
| `webAuthnDevices/{username}`                  | `GET` request to fetch device registration records for the user.
| `webAuthnDevices/{username}`                  | `DELETE` request to delete all device registration records for the user.
| `webAuthnDevices/{username}/{credential}`     | `DELETE` request to delete a device registration record for the user.
| `webAuthnDevices/{username}` | `POST` request to add a device registration record for the user with request body parameter `record`.

### Default

By default, a repository implementation is included that collects 
user device registrations and saves them into memory.
This option should only be used for demo and testing purposes.

### Others

Device registrations can also be managed using any one of the following strategies.

| Storage          | Description                                         
|--------------------------------------------------------------------------------------------------
| JSON     | [See this guide](FIDO2-WebAuthn-Authentication-Registration-JSON.html).  
| MongoDb     | [See this guide](FIDO2-WebAuthn-Authentication-Registration-MongoDb.html).  
| LDAP     | [See this guide](FIDO2-WebAuthn-Authentication-Registration-LDAP.html).  
| JPA     | [See this guide](FIDO2-WebAuthn-Authentication-Registration-JPA.html).  
| Redis     | [See this guide](FIDO2-WebAuthn-Authentication-Registration-Redis.html).  
| DynamoDb     | [See this guide](FIDO2-WebAuthn-Authentication-Registration-DynamoDb.html).
| REST     | [See this guide](FIDO2-WebAuthn-Authentication-Registration-Rest.html).


