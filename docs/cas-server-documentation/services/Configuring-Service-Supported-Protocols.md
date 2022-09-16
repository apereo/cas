---
layout: default
title: CAS - Configuring Service Protocols
category: Services
---

{% include variables.html %}

# Configure Service Protocols

Applications registered with CAS that intend to use the CAS protocol are given the option to indicate their support
for specific CAS protocol versions. If the service registration record declares support for specific CAS protocol versions, 
then attempts at validating service tickets using other CAS protocol versions would be blocked and should fail.

<div class="alert alert-info"><strong>Usage</strong>
<p>This feature specifically applies to CAS registered services applications.</p></div>

By default and for backward compatibility reasons, if no supported protocols are specified for the service 
registration record then all CAS protocol versions are allowed.

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://app.example.org.+",
  "name" : "App",
  "id" : 1,
  "supportedProtocols": [ "java.util.HashSet", ["CAS10", "CAS20"] ]
}
```
 
Accepted values are as follows:

| Storage          | Description                                         
|------------------------------------------------------------------------------------
| `SAML1`          | [See this guide](../protocol/SAML-Protocol.html).
| `CAS10`          | [See this guide](../protocol/CAS-Protocol.html).
| `CAS20`          | [See this guide](../protocol/CAS-Protocol.html).
| `CAS30`          | [See this guide](../protocol/CAS-Protocol.html).
