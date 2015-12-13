---
layout: default
title: CAS - OAuth Authentication
---

# OAuth/OpenID Authentication

<div class="alert alert-info"><strong>CAS as OAuth Server</strong><p>This page specifically describes how to enable OAuth/OpenID server support for CAS. If you would like to have CAS act as an OAuth/OpenID client communicating with other providers (such as Google, Facebook, etc), <a href="../integration/Delegate-Authentication.html">see this page</a>.</p></div>

To get a better understanding of the OAuth/OpenID protocol support in CAS, [see this page](../protocol/OAuth-Protocol.html).

## Configuration
Support is enabled by including the following dependency in the Maven WAR overlay:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-support-oauth</artifactId>
  <version>${cas.version}</version>
</dependency>
{% endhighlight %}

##Add OAuth Clients

Every OAuth client must be defined as a CAS service (notice the new *clientId* and *clientSecret* properties, specific to OAuth):

{% highlight json %}
{
  "@class" : "org.jasig.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "bypassApprovalPrompt": false,
  "serviceId" : "^(https|imaps)://hello.*",
  "name" : "HTTPS and IMAPS",
  "id" : 10000001
}
{% endhighlight %}

# OpenID Authentication

To configure CAS to act as an OpenID provider, please [see this page](../protocol/OpenID-Protocol.html).
