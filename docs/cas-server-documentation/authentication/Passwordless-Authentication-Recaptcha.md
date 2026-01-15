---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Passwordless Authentication - reCAPTCHA Integration

Passwordless authentication attempts can be protected and integrated
with [Google reCAPTCHA](https://developers.google.com/recaptcha). This requires
the presence of reCAPTCHA settings for the basic integration and instructing
the password management flow to turn on and verify requests via reCAPTCHA.

{% include_cached casproperties.html properties="cas.authn.passwordless.google-recaptcha" %}
