---
layout: default
title: CAS - Trusted Device Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Multifactor Authentication Trusted Device/Browser

In addition to triggers that are provided by the [MFA functionality](Configuring-Multifactor-Authentication.html) of CAS, there may be
cases where you wish to let the user decide if the current browser/device should be trusted so as to skip subsequent MFA requests. The
objective is for CAS to remember that decision for a configurable period of time and not bother the user with MFA until the decision
is either forcefully revoked or considered expired.

Trusting a device during an MFA workflow would mean that the ultimate decision is remembered for that **user** of that **location**
of that **device**. These keys are combined together securely and assigned to the final decision.

Before deployment, you should consider the following:

- Should users be optionally allowed to authorize the "current" device?
- ...or must that happen automatically once MFA is commenced?
- How should user decisions and choices be remembered? Where are they stored?
- How long should user decisions be trusted by CAS?
- How is a trusted authentication session communicated back to an application?

Note that enabling this feature by default means it's globally applied to all in the case if you have multiple MFA providers turned on.
This can be optionally disabled and applied only to a selected set of providers.

## Configuration

Support is provided via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-trusted-mfa" %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="multifactorTrustedDevices" %}

## Settings

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.core,cas.authn.mfa.trusted.crypto" %}

## Authentication Context

If an MFA request is bypassed due to a trusted authentication decision, applications will receive a special attribute as part of
the validation payload that indicates this behavior. Applications must further account for the scenario where they ask for an MFA
mode and yet don't receive confirmation of it in the response given the authentication session was trusted and MFA bypassed.

## Device Fingerprint

Please see [this guide](Multifactor-TrustedDevice-Authentication-DeviceFingerprint.html).

## Bypass

Please see [this guide](Multifactor-TrustedDevice-Authentication-Bypass.html).

## Storage

User decisions must be remembered and processed later on subsequent 
requests. A background *cleaner* process is also automatically scheduled to 
scan the chosen repository/database/registry periodically and remove expired records based on configured threshold parameters.

<div class="alert alert-warning"><strong>Cleaner Usage</strong><p>In a clustered CAS deployment, it is best to keep 
the cleaner running on one designated CAS 
node only and turn it off on all others via CAS settings. Keeping the cleaner running on all 
nodes may likely lead to severe performance and locking issues.</p></div>

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.cleaner" %}

### Default

If you do nothing, by default records are kept inside the runtime memory and cached for a configurable amount of time.
This is most useful if you have a very small deployment with a small user base or if you wish to demo the functionality.

### Others

Device registrations can also be managed using any one of the following strategies.

| Storage          | Description                                         
|--------------------------------------------------------------------------------------------------
| JSON     | [See this guide](Multifactor-TrustedDevice-Authentication-Storage-JSON.html).  
| JDBC     | [See this guide](Multifactor-TrustedDevice-Authentication-Storage-JDBC.html).  
| CouchDb     | [See this guide](Multifactor-TrustedDevice-Authentication-Storage-CouchDb.html).  
| MongoDb     | [See this guide](Multifactor-TrustedDevice-Authentication-Storage-MongoDb.html).  
| DynamoDb     | [See this guide](Multifactor-TrustedDevice-Authentication-Storage-DynamoDb.html).  
| Redis     | [See this guide](Multifactor-TrustedDevice-Authentication-Storage-Redis.html).  
| REST     | [See this guide](Multifactor-TrustedDevice-Authentication-Storage-Rest.html).  
