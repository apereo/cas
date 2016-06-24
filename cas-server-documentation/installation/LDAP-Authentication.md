---
layout: default
title: CAS - LDAP Authentication
---

# LDAP Authentication

LDAP integration is enabled by including the following dependency in the overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-ldap</artifactId>
     <version>${cas.version}</version>
</dependency>
```

## Configuration

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

## LDAP Password Policy Enforcement

The purpose of the LPPE is twofold:

- Detect a number of scenarios that would otherwise prevent user authentication, specifically 
using an Ldap instance as the primary source of user accounts.
- Warn users whose account status is near a configurable expiration date and redirect the flow 
to an external identity management system.

### Reflecting LDAP Account Status

The below scenarios are by default considered errors preventing authentication in a generic 
manner through the normal CAS login flow. LPPE intercepts the authentication flow, detecting the 
above standard error codes. Error codes are then translated into proper messages in the CAS login 
flow and would allow the user to take proper action, fully explaining the nature of the problem.

- `ACCOUNT_LOCKED`
- `ACCOUNT_DISABLED`
- `INVALID_LOGON_HOURS`
- `INVALID_WORKSTATION`
- `PASSWORD_MUST_CHANGE`
- `PASSWORD_EXPIRED`

The translation of LDAP errors into CAS workflow is 
all handled by [ldaptive](http://www.ldaptive.org/docs/guide/authentication/accountstate).

### Account Expiration Notification

LPPE is also able to warn the user when the account is about to expire. The expiration 
policy is determined through pre-configured Ldap attributes with default values in place.

<div class="alert alert-danger"><strong>No Password Management!</strong><p>LPPE is not 
about password management. If you are looking for that sort of capability integrating with CAS, you might be interested in:

<ul>
    <li><a href="http://code.google.com/p/pwm/">http://code.google.com/p/pwm/</a></li>‎
</ul></p></div>

## Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.ldaptive" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
```
