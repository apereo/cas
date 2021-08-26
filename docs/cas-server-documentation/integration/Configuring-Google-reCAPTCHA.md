---
layout: default
title: CAS - Google reCAPTCHA
category: Integration
---

{% include variables.html %}

# Google reCAPTCHA

reCAPTCHA is a [Google service](https://developers.google.com/recaptcha) that protects your CAS deployment from spam and abuse.
It uses advanced risk analysis techniques to tell humans and bots apart. CAS supports the reCAPTCHA API `v2` and `v3`.

Support is enabled by including the following module in the WAR Overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-captcha" %}

{% include casproperties.html properties="cas.google-recaptcha" %}

## Internet Explorer

Remember to disable Internet Explorer's "Compatibility View" mode. reCAPTCHA
may not render correctly when that mode is turned on.
