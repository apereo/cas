---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Scope Approval - OpenID Connect Authentication
      
The OpenID Connect specification indicates that once the user is 
authenticated, the Authorization Server (i.e. CAS) MUST obtain a decision 
before releasing information to the client application and relying party. This MAY be done through 
an interactive dialogue with the user that makes it clear what is being consented.

CAS requires the user to approve requested scopes by the client application. 
This is typically a one-time approval process that is remembered for subsequent authentication 
attempts for that *user and application*. The user is only prompted again if there is a change in the scopes 
requested by the client application or if the user clears their browser data.

You can turn off scope approval requests globally or per application.

{% include_cached casproperties.html properties="cas.authn.oauth.core" %}
    
## Storage

The storage mechanism for remembering scope approval decisions is the client browser's `IndexedDB`. There 
is no server-side storage of scope approval decisions and there is nothing sensitive in the data that is stored by the browser.
That said, data stored in `IndexedDB` uses `HmacSHA512` as a secret-sensitive one-way hashing technique to ensure integrity 
and prevent tampering.

`IndexedDB` is supported by recent versions of Chrome, Edge, Firefox, Safari and most modern mobile browsers.
However, very old browsers may not support `IndexedDB` or may support older/buggy versions. This matters most for old Android WebViews,
old iOS WebViews, old Safari, and legacy enterprise browsers.

## Bypass Approvals

You may turn off scope approval requests per client application by using the `bypassApprovalPrompt` setting: 

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client-id",
  "clientSecret": "secret",
  "serviceId": "^https://app.example.org/oidc",
  "name": "MyApplication",
  "id": 1,
  "bypassApprovalPrompt": true,
  "supportedResponseTypes": [ "java.util.HashSet", [ "code" ] ],
  "supportedGrantTypes": [ "java.util.HashSet", [ "authorization_code" ] ],
  "scopes" : [ "java.util.HashSet", [ "profile", "openid", "email" ] ]
}
```

## Custom

For more advanced scenarios, you can design your own resolver and register it with the CAS at runtime:

```java
@Bean
public ConsentApprovalViewResolver consentApprovalViewResolver() {
    return new CustomConsentApprovalViewResolver();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more
about how to register configurations into the CAS runtime.

