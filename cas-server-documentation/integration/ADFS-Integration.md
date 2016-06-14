---
layout: default
title: CAS - ADFS Integration
---

# Overview
The integration between the CAS Server and ADFS delegates user authentication from CAS Server to ADFS, making CAS Server a WS-Federation client. 
Claims released from ADFS are made available as attributes to CAS Server, and by extension CAS Clients.

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-wsfederation-webflow</artifactId>
  <version>${cas.version}</version>
</dependency>
```

## Components

### WsFed Configuration

Adjust and provide settings for the ADFS instance, and make sure you have obtained the ADFS signing certificate
and made it available to CAS at a location that can be resolved at runtime.

```properties
# The claim from ADFS that should be used as the user's identifier.
# cas.wsfed.idp.idattribute=upn
#
# Federation Service identifier
cas.wsfed.idp.id=
#
# The ADFS login url.
cas.wsfed.idp.url=
#
# Identifies resource(s) that point to ADFS's signing certificates.
# These are used verify the WS Federation token that is returned by ADFS.
# Multiple certificates may be separated by comma.
cas.wsfed.idp.signingcerts=classpath:adfs-signing.crt
#
# Unique identifier that will be set in the ADFS configuration.
# cas.wsfed.rp.id=urn:cas:localhost
#
# Slack dealing with time-drift between the ADFS Server and the CAS Server.
# cas.wsfed.idp.tolerance=10000

# cas.wsfed.idp.attribute.resolver.enabled=true
# cas.wsfed.idp.attribute.resolver.type=WSFED
```


### Modifying ADFS Claims
The WsFed configuration optionally may allow you to manipulate claims coming from ADFS but before they are inserted into the CAS user principal. For this to happen, you need
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

### Handling CAS Logout

An optional step, the `casLogoutView.html` can be modified to place a link to ADFS's logout page.

```html
<a href="https://adfs.example.org/adfs/ls/?wa=wsignout1.0">Logout</a>
```
