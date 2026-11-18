# Google reCAPTCHA

Source: https://apereo.github.io/cas/development/integration/Configuring-Google-reCAPTCHA.html

reCAPTCHA is a [Google service](https://developers.google.com/recaptcha) that protects your CAS deployment from spam and abuse. It uses advanced risk analysis techniques to tell humans and bots apart. CAS supports the reCAPTCHA API `v2` and `v3`.

Support is enabled by including the following module in the WAR Overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-captcha"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.google-recaptcha.enabled` (Required; type `Boolean`; default `true`): Whether reCAPTCHA should be enabled.
- `cas.google-recaptcha.score` (Required; type `Double`; default `0.5`): Google reCAPTCHA v3 returns a score (1.0 is very likely a good interaction, 0.0 is very likely a bot). reCAPTCHA learns by seeing real traffic on your site. For this reason, scores in a staging environment or soon after implementing may differ from production. As reCAPTCHA v3 doesn't ever interrupt the user flow, you can first run reCAPTCHA without taking action and then decide on thresholds by looking at your traffic in the admin console. By default, you can use a threshold of 0.5.
- `cas.google-recaptcha.secret` (Required; type `String`): The reCAPTCHA site secret.
- `cas.google-recaptcha.site-key` (Required; type `String`): The reCAPTCHA site key.
- `cas.google-recaptcha.activate-for-ip-address-pattern` (Optional; type `String`): A regular expression pattern to indicate that captcha should be activated when the remote IP address matches this pattern, and otherwise skipped and disabled.
- `cas.google-recaptcha.headers` (Optional; type `Map<String,String>`): Headers, defined as a `Map`, to include in the request when making the verification call to the recaptcha API.
- `cas.google-recaptcha.invisible` (Optional; type `Boolean`; default `false`): Whether google reCAPTCHA invisible should be enabled.
- `cas.google-recaptcha.position` (Optional; type `String`; default `bottomright`): The google reCAPTCHA badge position (only if invisible is enabled). Accepted values are: - `bottomright`: bottom right corner, default value. - `bottomleft`: bottom left corner - `inline`: allows one to control the CSS.
- `cas.google-recaptcha.verify-url` (Optional; type `String`; default `https://www.google.com/recaptcha/api/siteverify`): The reCAPTCHA endpoint for verification of the reCAPTCHA response. The endpoint is specific to the reCAPTCHA provider: - For Google reCAPTCHA, the endpoint is `https://www.google.com/recaptcha/api/siteverify`. - For hCaptcha, the endpoint is `https://hcaptcha.com/siteverify`. - For Turnstile, the endpoint is `https://challenges.cloudflare.com/turnstile/v0/siteverify`.
- `cas.google-recaptcha.version` (Optional; type `GoogleRecaptchaProperties.RecaptchaVersions`; default `GOOGLE_RECAPTCHA_V2`): Indicate the version of the recaptcha api. Available values are as follows: - `GOOGLE_RECAPTCHA_V2`: V2 version of the recaptcha API. - `GOOGLE_RECAPTCHA_V3`: V3 version of the recaptcha API. - `HCAPTCHA`: hCaptcha is an alternative to reCAPTCHA that requires website visitors to label images as part of its business model. The provider is more focused on manual image recognition challenges. It is a image classification task based CAPTCHA provider employing visual challenges like identifying objects. - `TURNSTILE`: Offers a CAPTCHA alternative by Cloudflare that prioritizes user experience and privacy. Turnstile delivers frustration-free, CAPTCHA-free web experiences to website visitors - with just a simple snippet of free code. Moreover, Turnstile stops abuse and confirms visitors are real without the data privacy concerns or awful user experience of CAPTCHAs. - `FRIENDLY_CAPTCHA`: Friendly captcha is a European alternative, accessible and GDPR compliant.

> **Use Case**
>
> Note that this particular integration applies to login and authentication attempts. Several other CAS modules do also support reCAPTCHA integrations for their own special flows, especially when it comes to reCAPTCHA activation strategies, separate from what is documented and available here.

## Internet Explorer

Remember to disable Internet Explorer’s “Compatibility View” mode. reCAPTCHA may not render correctly when that mode is turned on.

## reCAPTCHA Per Service

Certain reCAPTCHA settings can be defined on a per-service basis via [dedicated properties](https://apereo.github.io/cas/development/services/Configuring-Service-Custom-Properties.html):

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
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

The following properties are available and recognized by CAS for various modules and features:

| Name | Default Value | Type | Group |
| --- | --- | --- | --- |
| `captchaIPAddressPattern` | `true` | `SET` | `RECAPTCHA` |
| `captchaEnabled` | `true` | `BOOLEAN` | `RECAPTCHA` |

## Activation Strategy

reCAPTCHA activation strategy is generally controlled via CAS settings. It is also possible to design and inject your own activation strategy into CAS using the following `@Bean` that would be registered in a `@AutoConfiguration` class:

```java
@Bean
public CaptchaActivationStrategy captchaActivationStrategy() {
    return new MyCaptchaActivationStrategy();
}
```

Your configuration class needs to be registered with CAS. [See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) for better details.
