---
layout: default
title: CAS - Delegate Authentication Post Processing
category: Authentication
---

{% include variables.html %}

# Delegated Authentication Post Processing

Configured identity providers can go through a post-processing phase before they are presented to 
the user interface. Post processing operations have the ability to update and adjust the markup and
settings for a delegated identity provider to change the title, redirect URL, etc. A common example
for a post processor would be execute a server-side auto-redirect to 
the external identity provider conditionally, without having to wait for 
the browser to make that redirect.

{% include_cached casproperties.html properties="cas.authn.pac4j.core.groovy-provider-post-processor" %}

The outline of the Groovy script would be as follows:
                                                         
```groovy
import org.apereo.cas.web.*
import org.apereo.cas.web.support.*
import org.springframework.webflow.execution.*

def run(Object[] args) {
    def requestContext = args[0]
    def providers = (args[1] as Set<DelegatedClientIdentityProviderConfiguration>)
    def logger = args[2]
    
    def provider = providers[0]
    logger.info("Checking provider ${provider.name}...")
    def response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext)
    logger.debug("Redirecting to ${provider.redirectUrl}")
    response.sendRedirect(provider.redirectUrl);
}
```

The parameters passed are as follows:

| Parameter        | Description                                                                 |
|------------------|-----------------------------------------------------------------------------|
| `requestContext` | The object representing Spring Webflow's `RequestContext`.                  |
| `providers`      | The set of delegated identity provider configurations.                      |
| `logger`         | The object responsible for issuing log messages such as `logger.info(...)`. |

