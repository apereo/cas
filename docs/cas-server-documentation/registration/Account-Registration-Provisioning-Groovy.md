---
layout: default
title: CAS - Account Registration Provisioning
category: Registration
---
                  
{% include variables.html %}

# Account (Self-Service) Registration - Groovy Provisioning

Account registration requests can be submitted to an external Groovy script that 
is responsible for managing and storing the account in the appropriate systems of record.

{% include_cached casproperties.html properties="cas.account-registration.provisioning.groovy" %}

The script should match the following:

```groovy
import org.apereo.cas.acct.*
import org.springframework.context.ApplicationContext

def run(Object[] args) {
    def (registrationRequest,applicationContext,logger) = args
    
    logger.info("Provisioning account registration request ${registrationRequest}")
    return AccountRegistrationResponse.success()
}
```

The parameters passed are as follows:

| Parameter             | Description                                                                 |
|-----------------------|-----------------------------------------------------------------------------|
| `registrationRequest` | The object representing the account registration request.                   |
| `applicationContext`  | The object representing the Spring `ApplicationContext`.                    |
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
