---
layout: default
title: CAS - Apache Syncope Authentication
category: Authentication
---
{% include variables.html %}


# Apache Syncope Authentication

CAS support handling the authentication event via [Apache Syncope](https://syncope.apache.org/). This 
is done by using the `rest/users/self` REST API that is exposed by a running Syncope instance. 
As part of a successful authentication attempt, the properties of the provided user object 
are transformed into CAS attributes that can then be released to applications, etc.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-syncope-authentication" %}

{% include_cached casproperties.html properties="cas.authn.syncope" %}

## Attributes

As part of a successful authentication attempt, the following attributes 
provided by Apache Syncope are collected by CAS:

| Attribute Name                |
|-------------------------------|
| `syncopeUserRoles`            |
| `syncopeUserSecurityQuestion` |
| `syncopeUserStatus`           |
| `syncopeUserKey`              |
| `syncopeUserRealm`            |
| `syncopeUserCreator`          |
| `syncopeUserCreationDate`     |
| `syncopeUserChangePwdDate`    |
| `syncopeUserLastLoginDate`    |
| `syncopeUserDynRoles`         |
| `syncopeUserDynRealms`        |
| `syncopeUserMemberships`      |
| `syncopeUserDynMemberships`   |
| `syncopeUserDynRelationships` |
| `syncopeUserAttrs`            |

Note that attributes are only collected if they contain a value.

## Multitenancy

Configuration settings for database authentication can be specified in a multitenant environment.
Please [review this guide](../multitenancy/Multitenancy-Overview.html) for more information.

## Passwordless Authentication

The integration with Apache Syncope can also act as an account store
for [Passwordless Authentication](../authentication/Passwordless-Authentication-Storage-Syncope.html).
