---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Passwordless Authentication - Account Stores

User records that qualify for passwordless authentication must 
be found by CAS using one of the following strategies. All strategies may be configured
using CAS settings and are activated depending on the presence of configuration values.

| Option       | Description                                                                    |
|--------------|--------------------------------------------------------------------------------|
| Simple       | Please [see this guide](Passwordless-Authentication-Storage-Simple.html).      |
| MongoDb      | Please [see this guide](Passwordless-Authentication-Storage-MongoDb.html).     |
| LDAP         | Please [see this guide](Passwordless-Authentication-Storage-LDAP.html).        |
| JSON         | Please [see this guide](Passwordless-Authentication-Storage-JSON.html).        |
| Groovy       | Please [see this guide](Passwordless-Authentication-Storage-Groovy.html).      |
| REST         | Please [see this guide](Passwordless-Authentication-Storage-Rest.html).        |
| Custom       | Please [see this guide](Passwordless-Authentication-Storage-Custom.html).      |
| Duo Security | Please [see this guide](Passwordless-Authentication-Storage-DuoSecurity.html). |
      
Note that Multiple passwordless account stores can be used simultaneously to verify and locate passwordless accounts.
 
## Account Customization

When a passwordless account is located from store, it may be customized and post-processed to modify
various aspects of the account such as the requirement to activate MFA, password flows, etc. CAS allows
for a Groovy script that is passed the retrieved passwordless account and script is responsible for adjustments
and modifications.

```groovy
import org.apereo.cas.api.*

def run(Object[] args) {
    def (account,applicationContext,logger) = args

    logger.info("Customizing $account")
    
    // Update the account...
    
    return account
}
```

The following parameters are passed to the script:

| Parameter            | Description                                                                  |
|----------------------|------------------------------------------------------------------------------|
| `account`            | The object representing the `PasswordlessUserAccount` that is to be updated. |
| `applicationContext` | The object representing the Spring application context.                      |
| `logger`             | The object responsible for issuing log messages such as `logger.info(...)`.  |
                                                                              
Alternatively, you may build your own implementation of `PasswordlessUserAccountCustomizer` and register it as a Spring bean.

```java
@Bean
public PasswordlessUserAccountCustomizer myCustomizer() {
    return new MyPasswordlessUserAccountCustomizer();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.

## Disabling Passwordless Authentication Flow

Passwordless authentication can be disabled conditionally on a per-user basis. If
the passwordless account retrieved from the account store
carries a user whose `requestPassword` is set to `true`, the passwordless flow
(i.e. as described above with token generation, etc) will
be disabled and skipped in favor of the more usual CAS authentication flow,
challenging the user for a password. Support for this behavior may depend
on each individual account store implementation.
