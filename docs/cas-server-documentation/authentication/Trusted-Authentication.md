---
layout: default
title: CAS - Trusted Authentication
category: Authentication
---
{% include variables.html %}

# Trusted Authentication

The trusted authentication handler provides support for trusting authentication performed by some other component
in the HTTP request handling chain. Proxies (including Apache in a reverse proxy scenario) are the most common
components that perform authentication in front of CAS.

Trusted authentication handler support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-trusted-webflow" %}

Trusted authentication is able to extract the remote authenticated user via the following ways:

1. Username may be extracted from `HttpServletRequest#getRemoteUser()`
2. Username may be extracted from `HttpServletRequest#getUserPrincipal()`
3. Username may be extracted from a request header whose name is defined in CAS settings.

{% include_cached casproperties.html properties="cas.authn.trusted" %}

## Header Extraction

By default, request header names that match a certain regular expression pattern whose value also matches a defined pattern
are extracted as CAS attributes. For example, you may instruct CAS to extract header names that match the pattern `OPT_(.+)`
whose value is defined as `SYS_(.+)`. With this setup, if a request header is defined as `OPT_BUSINESS:SYS_ADMIN`, then final
CAS attribute that is extracted would be tagged under the attribute name `BUSINESS` with value(s) `ADMIN`. 

If you wish to create your own attribute extraction logic, you will need to
design a component and register it with CAS to handle the extraction task:

```java
package org.apereo.cas;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RemoteRequestPrincipalAttributesExtractor remoteRequestPrincipalAttributesExtractor() {
        return new MyRemoteRequestPrincipalAttributesExtractor();
    }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
