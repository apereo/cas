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

{% include {{ version }}/principal-transformation-configuration.md configKey="cas.authn.radius" %}

{% include {{ version }}/password-encoding-configuration.md configKey="cas.authn.radius" %}

{% include {{ version }}/radius-configuration.md configKey="cas.authn.radius" %}

{% include {{ version }}/radius-authentication-configuration.md %}

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

{% include {{ version }}/radius-configuration.md configKey="cas.authn.mfa.radius" %}

{% include {{ version }}/mfa-bypass-configuration.md configKey="cas.authn.mfa.radius" %}

## Configuration

{% include {{ version }}/radius-otp-authentication-configuration.md %}

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
