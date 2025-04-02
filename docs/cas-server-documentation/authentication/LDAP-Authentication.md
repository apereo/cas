---
layout: default
title: CAS - LDAP Authentication
category: Authentication
---
{% include variables.html %}


# LDAP Authentication

LDAP integration is enabled by including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-ldap" %}

## Configuration

CAS authenticates a username/password against an LDAP directory such as Active Directory or OpenLDAP.
There are numerous directory architectures and we provide configuration for four common cases.

Note that CAS will automatically create the appropriate components internally
based on the settings specified below. If you wish to authenticate against more than one LDAP
server, increment the index and specify the settings for the next LDAP server.

**Note:** Attributes retrieved as part of LDAP authentication are merged with all attributes
retrieved from other attribute repository sources, if any.
Attributes retrieved directly as part of LDAP authentication trump all other attributes.

{% include_cached casproperties.html properties="cas.authn.ldap" %}

## Password Policy Enforcement

To learn how to enforce a password policy for LDAP, please [review this guide](../installation/Password-Policy-Enforcement.html).

## Password Policy Enforcement
              
You may also be interested in synchronizing account passwords with one or more LDAP servers. To learn more, 
please [review this guide](../password_management/Password-Synchronization.html).
 
## Multitenancy

Configuration settings for LDAP authentication can be specified in a multitenant environment.
Please [review this guide](../multitenancy/Multitenancy-Overview.html) for more information.

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.ldaptive" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```
