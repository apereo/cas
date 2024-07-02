---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Passwordless Authentication - Delegated Authentication

Passwordless authentication can be integrated 
with [CAS delegated authentication](../integration/Delegate-Authentication.html). In this scenario,
once CAS configuration is enabled to support this behavior via settings or 
the located passwordless user account is considered *eligible* for delegated authentication,
CAS will allow passwordless authentication to skip its own *intended normal* 
flow (i.e. as described above with token generation, etc) in favor of 
delegated authentication that may be available and defined in CAS.

This means that if [delegated authentication providers](../integration/Delegate-Authentication.html) 
are defined and activated, CAS will skip 
its normal passwordless authentication flow in favor of the requested multifactor authentication 
provider and its flow. If no delegated identity providers 
are available, passwordless authentication flow will commence as usual.

The selection of a delegated authentication identity provider for a passwordless user is handled 
using a script. The script may be defined as such:

```groovy
def run(Object[] args) {
    def (passwordlessUser,clients,httpServletRequest,logger) = args
    logger.info("Testing username $passwordlessUser")
    clients[0]
}
``` 

The parameters passed are as follows:

| Parameter            | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `passwordlessUser`   | The object representing the `PasswordlessUserAccount`.                      |
| `clients`            | The object representing the collection of identity provider configurations. |
| `httpServletRequest` | The object representing the http request.                                   |
| `logger`             | The object responsible for issuing log messages such as `logger.info(...)`. |

The outcome of the script can be `null` to skip delegated authentication for 
the user, or it could a selection from the available identity providers passed into the script.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
