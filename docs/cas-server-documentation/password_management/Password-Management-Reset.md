---
layout: default
title: CAS - Password Management
category: Password Management
---

{% include variables.html %}

# Password Management - Password Reset

CAS may allow users to reset their passwords voluntarily. Those who have forgotten their account password
may receive a secure link with a time-based expiration policy at their registered email address and/or phone. The link
will allow the user to provide answers to his/her pre-defined security questions, which if successfully done,
will allow the user to next reset their password and login again. You may also specify a pattern for accepted passwords. 

By default, after a user has successfully changed their password they will be redirected to the login screen
to enter their new password and log in. CAS can also be configured to automatically log the user in after
a successful change. This behavior can be altered via CAS settings. 
   
CAS login requests also accept a special `doChangePassword` query parameter that allows one to forcefully launch into the
password reset flow. Specifying this parameter with a value of `true` can be particularly useful in the presence
of existing single sign-on sessions when the user who already has logged in wants to change their password.

## Configuration

{% include_cached casproperties.html properties="cas.authn.pm.reset" %}

To learn more about available notification options, please [see this guide](../notifications/SMS-Messaging-Configuration.html)
or [this guide](../notifications/Sending-Email-Configuration.html). 

### reCAPTCHA Integration

Password reset attempts can be protected and integrated
with [Google reCAPTCHA](https://developers.google.com/recaptcha). This requires
the presence of reCAPTCHA settings for the basic integration and instructing
the password management flow to turn on and verify requests via reCAPTCHA.

{% include_cached casproperties.html properties="cas.authn.pm.google-recaptcha" %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="passwordManagement" casModule="cas-server-support-pm-webflow" %}
