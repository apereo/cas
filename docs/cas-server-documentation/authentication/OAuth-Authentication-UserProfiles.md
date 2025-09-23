---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}

# User Profiles - OAuth Authentication

The requested user profile may be rendered and consumed by the application using the following options.
    
{% tabs oauthprofiles %}

{% tab oauthprofiles Nested %}

By default, the requested user profile is rendered using a `NESTED` format where
the authenticated principal and attributes are placed inside `id` and `attributes` tags
respectively in the final structure.

```json
{
  "id": "casuser",
  "attributes": {
    "email": "casuser@example.org",
    "name": "CAS"
  },
  "something": "else"
}
```

{% endtab %}

{% tab oauthprofiles Flat %}

This option flattens principal attributes by one degree, putting them
at the same level as `id`. Other nested elements in the final payload are left untouched.

```json
{
  "id": "casuser",
  "email": "casuser@example.org",
  "name": "CAS",
  "something": "else"
}
```

{% endtab %}

{% tab oauthprofiles Custom %}

If you wish to create your own profile structure, you will need to
design a component and register it with CAS to handle the rendering of the user profile:

```java
package org.apereo.cas.support.oauth;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyOAuthConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public OAuth20UserProfileViewRenderer oauthUserProfileViewRenderer() {
        ...
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.

{% endtab %}

{% endtabs %}

## Per Application

The user profile renderer may also be controlled on a per-application basis:

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "userProfileViewType": "FLAT"
}
```

