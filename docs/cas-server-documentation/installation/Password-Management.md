---
layout: default
title: CAS - Password Management
---

# Password Management

If authentication fails due to a rejected password policy, CAS is able to intercept
that request and allow the user to update the account password in place. The password management features of CAS are rather modest, and alternatively should the functionality provide inadequate for your policy, you may always redirect CAS to use a separate and standalone application that is fully in charge of managing the account password and associated flows.

CAS may also allow users to reset their passwords voluntarily. Those who have forgotten their account password
may receive a secure link with a time-based expiration policy at their registered email address and/or phone. The link
will allow the user to provide answers to his/her pre-defined security questions, which if successfully done,
will allow the user to next reset their password and login again. You may also specify a pattern for accepted passwords. 

By default, after a user has successfully changed their password they will be redirected to the login screen
to enter their new password and log in. CAS can also be configured to automatically log the user in after
a successful change. The `autoLogin` flag in [password management properties](Configuration-Properties.html#password-management)
controls this behavior.

To learn more about available notification options, please [see this guide](SMS-Messaging-Configuration.html) or [this guide](Sending-Email-Configuration.html).

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#password-management).

## JSON

To learn more about available notification options, please [see this guide](Password-Management-JSON.html).

## LDAP

To learn more about available notification options, please [see this guide](Password-Management-LDAP.html).

## JDBC

To learn more about available notification options, please [see this guide](Password-Management-JDBC.html).

## REST

To learn more about available notification options, please [see this guide](Password-Management-REST.html).

## Custom

To learn more about available notification options, please [see this guide](Password-Management-Custom.html).
