---
layout: default
title: CAS - Google reCAPTCHA
category: Integration
---

{% include variables.html %}

# Google reCAPTCHA

reCAPTCHA is a [Google service](https://developers.google.com/recaptcha) that 
protects your CAS deployment from spam and abuse.
It uses advanced risk analysis techniques to tell humans and bots 
apart. CAS supports the reCAPTCHA API `v2` and `v3`.

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-captcha" %}

{% include_cached casproperties.html properties="cas.google-recaptcha" %}

<div class="alert alert-info"><strong>Use Case</strong><p>Note that this particular integration
applies to login and authentication attempts. Several other CAS modules do also support reCAPTCHA
integrations for their own special flows, specially when it comes to reCAPTCHA activation strategies,
separate from what is documented and available here.</p></div>

## Internet Explorer

Remember to disable Internet Explorer's "Compatibility View" mode. reCAPTCHA
may not render correctly when that mode is turned on.

## reCAPTCHA Per Service

Certain reCAPTCHA settings can be defined on a per-service 
basis via [dedicated properties](../services/Configuring-Service-Custom-Properties.html):

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "properties" : {
    "@class" : "java.util.HashMap",
    "captchaEnabled" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "false" ] ]
    },
    "captchaIPAddressPattern" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "pattern1", "pattern2" ] ]
    }
  }
}
```

{% include_cached registeredserviceproperties.html groups="RECAPTCHA" %}

## Activation Strategy

reCAPTCHA activation strategy is generally controlled via CAS settings. It is also possible to design and 
inject your own activation strategy into CAS using the following `@Bean` that would 
be registered in a `@Configuration` class:

```java
@Bean
public CaptchaActivationStrategy captchaActivationStrategy() {
    return new MyCaptchaActivationStrategy();
}
```

Your configuration class needs to be registered
with CAS. [See this guide](../configuration/Configuration-Management-Extensions.html) for better details.
