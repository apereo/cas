---
layout: default
title: CAS - RADIUS Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# RADIUS Authentication

RADIUS support is enabled by only including the following dependency in the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-radius" %}

## Configuration

{% include casproperties.html modules="cas-server-support-radius" properties="cas.authn.radius" %}

You may also need to declare the following repository in
your CAS overlay to be able to resolve dependencies:

```groovy       
repositories {
    maven { 
        mavenContent { releasesOnly() }
        url "https://dl.bintray.com/apereocas/jradius" 
    }
}
```

# RSA RADIUS MFA

RSA RADIUS OTP support for MFA is enabled by only including the following dependency in the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-radius-mfa" %}

{% include casproperties.html
modules="cas-server-support-radius-mfa"
properties="cas.authn.mfa.radius" %}

## Repository

You may also need to declare the following repository in
your CAS overlay to be able to resolve dependencies:

```xml 
repositories {
    maven { 
        mavenContent { releasesOnly() }
        url "https://jitpack.io" 
    }
}
```
