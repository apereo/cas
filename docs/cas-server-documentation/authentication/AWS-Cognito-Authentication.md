---
layout: default
title: CAS - Amazon Cognito Authentication
category: Authentication
---
{% include variables.html %}


# Amazon Cognito Authentication

Verify and authenticate credentials using [Amazon Cognito](https://aws.amazon.com/cognito/).

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-aws-cognito-authentication" %}

## Configuration

{% include {{ version }}/principal-transformation-configuration.md configKey="cas.authn.cognito" %}

{% include {{ version }}/password-encoding-configuration.md configKey="cas.authn.cognito" %}

{% include {{ version }}/aws-configuration.md configKey="cas.authn.cognito" %}

{% include {{ version }}/aws-cognito-authentication-configuration.md %}

When you create the *app client* entry in the Amazon Cognito management console, make sure the app is able to support the `ADMIN_NO_SRP_AUTH` authentication flow and it is *NOT* assigned a secret key.

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.amazonaws" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
...
```
