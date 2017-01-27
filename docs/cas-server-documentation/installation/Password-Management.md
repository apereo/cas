---
layout: default
title: CAS - Password Management
---

# Password Management

CAS presents humble password management features. If authentication fails due to a rejected password policy, CAS is able to intercept
that request and allow the user to update the account password in place. The password management features of CAS are rather modest, and
alternatively should the functionality provide inadequate for your policy, you may always redirect CAS to use a separate and standalone
application that is fully in charge of managing the account password and associated flows.

CAS may also allow users to reset their passwords voluntarily. Those who have forgotten their account password
may receive a secure link with a time-based expiration policy at their registered email address. The link
will allow the user to provide answers to his/her pre-defined security questions, which if successfully done,
will allow the user to next reset their password and login again.

This functionality needs to be explicitly enabled in CAS settings. You may also specify a pattern for accepted passwords. 
To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#password-management).

## LDAP

The updated password may be stored inside an LDAP server.
To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

## JDBC

The updated password may be stored inside a database.
To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).
