---
layout: default
title: CAS - ADFS Integration
---

# Overview

The integration between the CAS Server and ADFS delegates user authentication from CAS Server 
to ADFS, making CAS Server a WS-Federation client. 
Claims released from ADFS are made available as attributes to CAS Server, and by extension CAS Clients.

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-wsfederation-webflow</artifactId>
  <version>${cas.version}</version>
</dependency>
```


## WsFed Configuration

Adjust and provide settings for the ADFS instance, and make sure you have obtained the ADFS signing certificate
and made it available to CAS at a location that can be resolved at runtime.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

## Modifying ADFS Claims
The WsFed configuration optionally may allow you to manipulate claims coming from ADFS but 
before they are inserted into the CAS user principal. For this to happen, you need
to put together an implementation of `WsFederationAttributeMutator` that changes and manipulates ADFS claims:

```java
package org.apereo.cas.support.wsfederation;

@Component("wsfedAttributeMutator")
public class WsFederationAttributeMutatorImpl implements WsFederationAttributeMutator {
    public void modifyAttributes(...) {
        ...
    }
}
```

Finally, ensure that the attributes sent from ADFS are available and mapped in
your `attributeRepository` configuration.

## Handling CAS Logout

An optional step, the `casLogoutView.html` can be modified to place a link to ADFS's logout page.

```html
<a href="https://adfs.example.org/adfs/ls/?wa=wsignout1.0">Logout</a>
```

## Per-Service Relying Party Id

In order to specify a relying party identifier per service definition, adjust your service
registry to match the following:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "properties" : {
    "@class" : "java.util.HashMap",
    "wsfed.relyingPartyIdentifier" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "custom-identifier" ] ]
    }
  }
}
```
