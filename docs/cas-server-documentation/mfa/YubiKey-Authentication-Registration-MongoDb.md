---
layout: default
title: CAS - YubiKey Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# MongoDb YubiKey Registration

Support is enabled by including the following dependencies in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-yubikey-mongo" %}

{% include casproperties.html properties="cas.authn.mfa.yubikey.mongo" %}

The registration records are kept inside a single MongoDb collection of your choosing that will be auto-created by CAS. The structure of this collection is as follows:

| Field              | Description
|--------------------------------------------------------------------------------------
| `id`               | Unique record identifier, acting as the primary key.
| `publicId`         | The public identifier/key of the device used for authentication.
| `username`         | The username whose device is registered.
