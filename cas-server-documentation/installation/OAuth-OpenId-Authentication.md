---
layout: default
title: CAS - OAuth Authentication
---

# OAuth/OpenID Authentication

<div class="alert alert-info"><strong>CAS as OAuth Server</strong><p>This page specifically describes how to enable OAuth/OpenID server support for CAS. If you would like to have CAS act as an OAuth/OpenID client communicating with other providers (such as Google, Facebook, etc), <a href="../integration/Delegate-Authentication.html">see this page</a>.</p></div>

To get a better understanding of the OAuth/OpenID protocol support in CAS, [see this page](../protocol/OAuth-Protocol.html).

## Configuration
Support is enabled by including the following dependency in the Maven WAR overlay:

```xml
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-support-oauth</artifactId>
  <version>${cas.version}</version>
</dependency>
```

After enabling OAuth support, three new urls will be available:

* **/oauth2.0/authorize**  
It's the url to call to authorize the user: the CAS login page will be displayed and the user will authenticate. Required input GET parameters: *client_id* and *redirect_uri*.

* **/oauth2.0/accessToken**  
It's the url to call to exchange the code for an access token. Required input GET parameters: *client_id*, *redirect_uri*, *client_secret* and *code*.

* **/oauth2.0/profile**  
It's the url to call to get the profile of the authorized user. Required input GET parameter: *access_token*. The response is in JSON format with all attributes of the user.


## Grant types

Two grant types are supported:

### The authorization code grant type has three steps:

1) `/cas/oauth2.0/authorize?response_type=code&client_id=ID&redirect_uri=CALLBACK` returns the code as a parameter of the CALLBACK url

2) `/cas/oauth2.0/accessToken?client_id=ID&client_secret=SECRET&code=CODE&redirect_uri=CALLBACK` returns the access token

3) `/cas/oauth2.0/profile?access_token=TOKEN` returns the user profile.

### The implicit grant type has two steps:

1) `/cas/oauth2.0/authorize?response_type=token&client_id=ID&redirect_uri=CALLBACK` returns the access token as an anchor parameter of the
 CALLBACK url

2) `/cas/oauth2.0/profile?access_token=TOKEN` returns the user profile.


## Add OAuth Clients

Every OAuth client must be defined as a CAS service (notice the new *clientId* and *clientSecret* properties, specific to OAuth):

```json
{
  "@class" : "org.jasig.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "bypassApprovalPrompt": false,
  "serviceId" : "^(https|imaps)://hello.*",
  "name" : "HTTPS and IMAPS",
  "id" : 10000001
}
```

## OAuth Code Expiration Policy

The expiration policy for OAuth codes are controlled by the following properties:

```properties
# oauth.code.numberOfUses=1
# oauth.code.timeToKillInSeconds=1
```


## OAuth Access Token Expiration Policy

The expiration policy for OAuth access tokens are controlled by the following properties:

```properties
# oauth.access.token.maxTimeToLiveInSeconds=28800
# oauth.access.token.timeToKillInSeconds=7200
```

# OpenID Authentication

To configure CAS to act as an OpenID provider, please [see this page](../protocol/OpenID-Protocol.html).
