---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}

# Response Mode - OAuth Authentication

Every OAuth relying party can define its own required response mode:

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "supportedGrantTypes": [ "java.util.HashSet", [ "...", "..." ] ],
  "supportedResponseTypes": [ "java.util.HashSet", [ "...", "..." ] ]
  "responseMode": "..."
}
```

Response mode variations allow CAS to alter the mechanism used for returning responses back to the client. 

{% tabs oauthresponsemodes %}

{% tab oauthresponsemodes Query %}

The `query` authorization response parameters are included in the query component of the redirect URI as query parameters.

```
https://example.com/cb?code=SplxlOBeZQQYbYS6WxSbIA&state=xyz
```

In the above example, `code` and `state` are the authorization response parameters included in the query component of the URI.

This response mode is commonly used in front-channel communication, 
where the authorization response is sent back to the client through the user's 
browser.

{% endtab %}

{% tab oauthresponsemodes Fragment %}

The `fragment` authorization response parameters are included in the fragment component of the 
redirect URI. The fragment component is the part of the URI that comes after the `#` symbol. For example:

```
https://example.com/cb#access_token=SlAV32hkKG&token_type=bearer&expires_in=3600&state=xyz
```

This response mode is often used when the communication between the client and CAS
occurs in a front-channel where JavaScript can access the fragment of the URI.

{% endtab %}

{% tab oauthresponsemodes Form Post %}

In `form_post` response mode, CAS sends the response parameters as HTML form parameters in the body of an HTTP `POST` request. 
This request is submitted by the client browser and is sent to the client's redirection URI.
     
```html
<html>
<body onload="document.oauth.submit();" style="display:none">
    <form name="oauth" action="https://client.example.com/cb" method="post">
      <input type="hidden" name="code" value="SplxlOBeZQQYbYS6WxSbIA">
      <input type="hidden" name="state" value="xyz">
      <input type="submit" value="Continue">
    </form>
</body>
</html>
```

<div class="alert alert-info">:information_source: <strong>Browser Support</strong><p>
Needless to say, this response mode requires the client browser to support and enable JavaScript
so the HTML form can be automatically and invisibly submitted.
</p></div>

The response mode is useful when a secure front-channel communication is required, and 
the client is capable of receiving and processing `POST` requests.

{% endtab %}

{% endtabs %}
