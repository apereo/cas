---
layout: default
title: CAS - Digest Authentication
category: Authentication
---
{% include variables.html %}


# Digest Authentication

Digest authentication is one of the agreed-upon methods CAS can use to negotiate credentials with a user's
web browser. This can be used to confirm the identity of a user before sending sensitive information.
It applies a hash function to the username and password before sending them over the network.
Technically, digest authentication is an application of MD5 cryptographic
hashing with usage of nonce values to prevent replay attacks. It uses the HTTP protocol.

<div class="alert alert-warning"><strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future.</strong></p>
</div>

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-digest-authentication" %}

For additional information on how digest authentication works,
please [review this guide](https://en.wikipedia.org/wiki/Digest_access_authentication).

## Configuration

{% include_cached casproperties.html properties="cas.authn.digest" %}

## Credential Management

By default, CAS attempts to cross-check computed hash values against what the client reports in the authentication request.
In order for this to succeed, CAS will need access to the data store where MD5 representations of credentials are kept. The store
needs to keep the hash value at a minimum of course.

By default, CAS uses its properties file to house the hashed credentials. Real production-level deployments
of this module will need to provide their own data store that provides a collection of hashed values as authenticating accounts.

## Client Requests

The following snippets demonstrate how a given Java client may use CAS digest authentication,
via Apache's HttpClient library:

```java
var target = new HttpHost("localhost", 8080, "http");

var credsProvider = new BasicCredentialsProvider();
credsProvider.setCredentials(
    new AuthScope(target.getHostName(), target.getPort()),
    new UsernamePasswordCredentials("casuser", "Mellon"));

var httpclient = HttpClients.custom()
        .setDefaultCredentialsProvider(credsProvider)
        .build();

try {
    var httpget = new HttpGet("http://localhost:8080/cas/login");

    // Create AuthCache instance
    var authCache = new BasicAuthCache();

    // Generate DIGEST scheme object, initialize it and add it to the local auth cache
    var digestAuth = new DigestScheme();
    digestAuth.overrideParamter("realm", "CAS");
    authCache.put(target, digestAuth);

    // Add AuthCache to the execution context
    var localContext = HttpClientContext.create();
    localContext.setAuthCache(authCache);

    System.out.println("Executing request " + httpget.getRequestLine() + " to " + target);
    try (var response = httpclient.execute(target, httpget, localContext)) {
        System.out.println(response.getStatusLine());
        System.out.println(EntityUtils.toString(response.getEntity()));
    }
} finally {
    httpclient.close();
}
```
