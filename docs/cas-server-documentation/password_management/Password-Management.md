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

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pm-webflow" bundled="true" %}

<div class="alert alert-info">:information_source: <strong>YAGNI</strong><p>You do not need to explicitly include this module
in your configuration and overlays. This is just to tell you that it exists.</p></div>

## Configuration

To learn more about available notification options, please [see this guide](../notifications/SMS-Messaging-Configuration.html) 
or [this guide](../notifications/Sending-Email-Configuration.html). 

{% include_cached casproperties.html properties="cas.authn.pm" includes=".core,.webflow" %}

## Password Reset

Please [see this guide](../password_management/Password-Management-Reset.html) for more details.

## Account Management

CAS may also allow individual end-users to update certain aspects of their account that relate to
password management in a *mini portal* like setup, such as resetting the password or updating security questions,
etc. Please [see this guide](../registration/Account-Management-Overview.html) for more details.
 
## Forgot Username
                                                                      
To learn more, please [see this guide](Password-Management-ForgotUsername.html).

## Password History

To learn more, please [see this guide](Password-Management-History.html).
   
## Storage

User accounts can be found via the following ways.

| Storage          | Instructions                                         
|------------------------------------------------------------------------------------
| JSON             | [See this guide](Password-Management-JSON.html).
| Groovy           | [See this guide](Password-Management-Groovy.html).
| LDAP             | [See this guide](Password-Management-LDAP.html).
| JDBC             | [See this guide](Password-Management-JDBC.html).
| REST             | [See this guide](Password-Management-REST.html).
| Custom           | [See this guide](Password-Management-Custom.html).
