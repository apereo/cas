---
layout: default
title: CAS - Password Management
category: Password Management
---

{% include variables.html %}

# Password Management

If authentication fails due to a rejected password policy, CAS is able to intercept
that request and allow the user to update the account password in place. The password management 
features of CAS are rather modest, and alternatively should the functionality provide inadequate 
for your policy, you may always redirect CAS to use a separate and standalone application 
that is fully in charge of managing the account password and associated flows.

CAS may also allow users to reset their passwords voluntarily. Those who have forgotten their account password
may receive a secure link with a time-based expiration policy at their registered email address and/or phone. The link
will allow the user to provide answers to his/her pre-defined security questions, which if successfully done,
will allow the user to next reset their password and login again. You may also specify a pattern for accepted passwords. 

By default, after a user has successfully changed their password they will be redirected to the login screen
to enter their new password and log in. CAS can also be configured to automatically log the user in after
a successful change. This behavior can be altered via CAS settings. 

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-pm-webflow" %}

<div class="alert alert-info"><strong>YAGNI</strong><p>You do not need to explicitly include this module
in your configuration and overlays. This is just to teach you that it exists.</p></div>

## Configuration

To learn more about available notification options, please [see this guide](../notifications/SMS-Messaging-Configuration.html) 
or [this guide](../notifications/Sending-Email-Configuration.html). 

{% include casproperties.html properties="cas.authn.pm.core,cas.authn.pm.webflow" %}

### Password Reset

{% include casproperties.html properties="cas.authn.pm.reset" %}

### reCAPTCHA Integration

Password reset attempts can be protected and integrated 
with [Google reCAPTCHA](https://developers.google.com/recaptcha). This requires 
the presence of reCAPTCHA settings for the basic integration and instructing 
the password management flow to turn on and verify requests via reCAPTCHA. 

{% include casproperties.html properties="cas.authn.pm.google-recaptcha" %}
 
## Forgot Username
                                                                      
CAS provides the ability to retrieve a forgotten username. This behavior is tightly integrated with the password
management functionality where each account storage service is allowed to retrieve the user record using a supplied
identifier from the user (typically an email address) and supply the username via email notifications, etc.

{% include casproperties.html properties="cas.authn.pm.forgot-username" %}

## Password History

To learn more, please [see this guide](Password-Management-History.html).

## JSON Storage

Accounts and password may be stored inside a static modest JSON resource. This option is most useful during development and 
for demo purposes. To learn more, please [see this guide](Password-Management-JSON.html).

## Groovy Storage

Accounts and password may be handled and calculated via a Groovy script. To learn more, 
please [see this guide](Password-Management-Groovy.html).

## LDAP Storage

The account password and security questions may be stored inside an LDAP server. To learn more, 
please [see this guide](Password-Management-LDAP.html).

## JDBC Storage

The account password and security questions may be stored inside a relational database. To learn more, 
please [see this guide](Password-Management-JDBC.html).

## REST Storage

The account password and security questions can also be managed using a REST API. To learn more 
please [see this guide](Password-Management-REST.html).

## Custom

To design your own password management storage options and strategy, 
please [see this guide](Password-Management-Custom.html).
