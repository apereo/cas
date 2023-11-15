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
    def (requestContext,providers,logger) = args
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

    
## Preprocessing Authentication

You may also opt into preprocessing the delegated authentication event prior to the final step in the process and before an SSO session is created. 
Preprocessing the authentication here allows one to manipulate and update the final authenticated principal before it's fully baked into the SSO session.
This can be done using the following bean definition and by implementing `DelegatedAuthenticationPreProcessor`:

```java
@Bean
public DelegatedAuthenticationPreProcessor myProcessor() {
    return new MyDelegatedAuthenticationPreProcessor();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.
