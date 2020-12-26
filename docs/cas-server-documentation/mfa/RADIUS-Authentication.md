---
layout: default
title: CAS - RADIUS Authentication
category: Multifactor Authentication
---

# RADIUS Authentication

RADIUS support is enabled by only including the following dependency in the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-radius" %}

## Configuration

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#radius-authentication).

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

## Configuration

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#radius-otp).

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
