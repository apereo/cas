---
layout: default
title: CAS - YubiKey Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# JPA YubiKey Registration

Support is enabled by including the following dependencies in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-yubikey-jpa" %}

{% include casproperties.html properties="cas.authn.mfa.yubikey.jpa" %}

The expected database schema that is automatically created and configured by CAS contains a single table as `YubiKeyAccount` with the following fields:

| Field              | Description
|--------------------------------------------------------------------------------------
| `id`               | Unique record identifier, acting as the primary key.
| `publicId`         | The public identifier/key of the device used for authentication.
| `username`         | The username whose device is registered.
