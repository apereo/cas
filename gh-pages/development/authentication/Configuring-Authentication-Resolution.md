---
layout: default
title: CAS - Configuring Authentication Resolution
category: Authentication
---
{% include variables.html %}


# Authentication Resolution Strategy

Authentication handlers are typically defined globally and then executed and tried by the authentication engine. 
The collection of handlers can be narrowed down using a selection criteria that defines an eligibility criteria
for the handler based on the current authentication transaction, provided credentials, service definition policy
and many other controls.

{% include casproperties.html
properties="cas.authn.core.groovy-authentication-resolution,cas.authn.core.service-authentication-resolution" %}


## Per Registered Service

Each registered application in the registry may be assigned a set of identifiers/names for the required authentication 
handlers available and configured in CAS. These names can be used to enforce a service definition to only use the 
authentication strategy carrying that name when an authentication request is submitted to CAS.

Please [review this guide](../services/Configuring-Service-AuthN-Policy.html) to learn more.

## Groovy Script

The global collection of authentication handlers can also pass through a Groovy script as a filter
to narrow down the list of candidates for the current transaction. The outline of the script may be designed as:

```groovy
def run(Object[] args) {
    def handlers = args[0]
    def transaction = args[1]
    def servicesManager = args[2]
    def logger = args[3]

    return handlers
}

def supports(Object[] args) {
    def handlers = args[0]
    def transaction = args[1]
    def servicesManager = args[2]
    def logger = args[3]
    true
}
```

The following parameters are passed to the script:

| Parameter        | Description
|------------------|--------------------------------------------------------------------------------------------
| `handlers`       | Collection of available and candidate `AuthenticationHandler`s.
| `transaction`    | The authentication transaction currently in progress.
| `servicesManager`  | The `ServicesManager` object responsible for locating service definitions attached to this transaction.
| `logger`           | The object responsible for issuing log messages such as `logger.info(...)`.

The outcome of the script should be the collection of selected handlers with the type `Set<AuthenticationHandler>`.
