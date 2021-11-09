---
layout: default
title: CAS - U2F - FIDO Universal 2nd Factor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# U2F - FIDO Universal Authentication

U2F is an open authentication standard that enables internet users to securely 
access any number of online services, with one single device, instantly 
and with no drivers, or client software needed. The CAS U2F implementation 
is built on top of [Yubico](https://www.yubico.com/about/background/fido/) and 
the technical specifications are hosted by the open-authentication 
industry consortium known as the [FIDO Alliance](https://fidoalliance.org/).

Note that not all browsers today support U2F. While support in recent versions of Chrome and 
Opera seem to exist, you should always verify that U2F support is available for your target browser.

Support is enabled by including the following module in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-u2f" %}

{% include_cached casproperties.html properties="cas.authn.mfa.u2f.core" %}


## Registration

U2F device registration flows are baked into CAS automatically. A 
background *cleaner* process is also automatically scheduled to scan the 
repository periodically and remove expired device registration records 
based on configured parameters. In the default setting U2F devices
expire after a fixed period since a user registered the U2F token 
(independent of the last time the token was used); if you deploy U2F
MFA for a setup where tokens are centrally distributed and revoked, 
you may want to extend the interval.

{% include_cached casproperties.html properties="cas.authn.mfa.u2f.cleaner" %}

<div class="alert alert-warning"><strong>Cleaner Usage</strong><p>In a 
clustered CAS deployment, it is best to keep the cleaner running on one designated 
CAS node only and turn it off on all others via CAS settings. Keeping the 
cleaner running on all nodes may likely lead to severe performance and locking issues.</p></div>

### Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="u2fDevices" %}

### Default

By default, a repository implementation is included that collects 
user device registrations and saves them into memory.
This option should only be used for demo and testing purposes.

### JSON
      
Please [see this guide](FIDO-U2F-Authentication-JSON.html) for more info.

### Groovy

Please [see this guide](FIDO-U2F-Authentication-Groovy.html) for more info.

### JPA

Please [see this guide](FIDO-U2F-Authentication-JPA.html) for more info.

### MongoDb

Please [see this guide](FIDO-U2F-Authentication-MongoDb.html) for more info.

### DynamoDb

Please [see this guide](FIDO-U2F-Authentication-DynamoDb.html) for more info.

### Redis

Please [see this guide](FIDO-U2F-Authentication-Redis.html) for more info.

### CouchDb

Please [see this guide](FIDO-U2F-Authentication-CouchDb.html) for more info.

### REST

Please [see this guide](FIDO-U2F-Authentication-Rest.html) for more info.
