---
layout: default
title: CAS - Delegate Authentication
category: Authentication
---

{% include variables.html %}

# Delegated Authentication - Identity Provider Registration

An identity provider is a server which can authenticate users (like Google, Yahoo...) instead of a CAS server.
If you want to delegate the CAS authentication to Twitter for example, you have to add an
OAuth client for the Twitter provider, which will be done automatically for you once provider settings are taught to CAS.

Notice that for each OAuth provider, the CAS server is considered as an OAuth client and therefore should be declared as
an OAuth client at the OAuth provider. After the declaration, a key and a secret is given by the OAuth provider which has
to be defined in the CAS configuration as well.

## Default

{% assign providers = "DropBox,Facebook,FourSquare,Google,HiOrgServer,PayPal,Twitter,WindowsLive,Wordpress,Yahoo,CAS,LinkedIn,GitHub,OAuth20,Google-OpenID-Connect,SAML,Keycloak,Azure-AD,Apple,Generic-OpenID-Connect" | split: "," | sort %}

Identity providers for delegated authentication can be registered with CAS using settings. 

<table class="cas-datatable">
  <thead>
    <tr><th>Provider</th><th>Reference</th></tr>
  </thead>
  <tbody>
    {% for provider in providers %}
    <tr>
    <td>{{ provider | replace: "-", " " }} </td>
    <td><a href="Delegate-Authentication-{{ provider }}.html">See this guide</a>.</td>
    </tr>
    {% endfor %}
  </tbody>
</table>

## REST

Identity providers for delegated authentication can be provided to CAS using an external REST endpoint. Note that the results of the 
REST endpoint are cached by CAS using a configurable expiration policy, and the endpoint is only contacted by CAS if the cache content
is empty or has been invalidated.

{% include_cached casproperties.html properties="cas.authn.pac4j.rest" %}

The expected payload *type*, that is controlled via CAS settings, can be understood and consumed in the following ways.

### Pac4j Payload

This allows the CAS server to reach to a remote REST endpoint whose responsibility is to produce the following payload in the response body:

```json
{
    "callbackUrl": "https://sso.example.org/cas/login",
    "properties": {
        "github.id": "...",
        "github.secret": "...",
        
        "cas.loginUrl.1": "...",
        "cas.protocol.1": "..."
    }
}
```

The syntax and collection of available `properties` in the above payload is controlled by the [Pac4j library](https://github.com/pac4j/pac4j). 
The response that is returned must be accompanied by a `200` status code.

### CAS Payload

This allows the CAS server to reach to a remote REST endpoint whose responsibility is to produce the following payload in the response body:

```json
{
    "cas.authn.pac4j.github.client-name": "...",
    "cas.authn.pac4j.github.id": "...",
    "cas.authn.pac4j.github.secret": "...",
    
    "cas.authn.pac4j.cas[0].login-url": "...",
    "cas.authn.pac4j.cas[0].protocol": "..."
}
```

The payload is expected to contain CAS specific properties that would be used to construct external identity providers. The 
response that is returned must be accompanied by a `200` status code.
