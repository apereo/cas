---
layout: default
title: CAS - U2F - FIDO Universal Multifactor Authentication
category: Multifactor Authentication
---

# FIDO2 WebAuthn Multifactor Authentication

Support is enabled by including the following module in the WAR overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-webauthn</artifactId>
     <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#fido2-webauthn).

## Registration

Device registration flows are baked into CAS automatically. A background *cleaner* process is also automatically scheduled to scan the 
repository periodically and remove expired device registration records based on configured parameters. In the default setting, devices
expire after a fixed period since a user registered their device. If you deploy U2F
MFA for a setup where tokens are centrally distributed and revoked, 
you may want to [extend the interval](../configuration/Configuration-Properties.html#fido2-webauthn).

<div class="alert alert-warning"><strong>Cleaner Usage</strong><p>In a clustered CAS deployment, it is best to keep 
the cleaner running on one designated CAS node only and turn it off on all others via CAS settings. Keeping the cleaner running 
on all nodes may likely lead to severe performance and locking issues.</p></div>

### Administrative Endpoints

The following endpoints are provided by CAS:
 
| Endpoint                  | Description
|---------------------------|------------------------------------------------


### Default

By default, a repository implementation is included that collects user device registrations and saves them into memory.
This option should only be used for demo and testing purposes.

### JSON

A device repository implementation that collects user device registrations and saves them into a JSON file whose 
path is taught to CAS via settings. This is a very modest option and should mostly be used for demo and testing 
purposes. Needless to say, this JSON resource acts as a database that must be available to all CAS server nodes in the cluster.

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#fido2-webauthn-json).

