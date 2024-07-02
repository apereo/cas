---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Groovy Passwordless Authentication Storage

This strategy allows one to locate user records via a Groovy script. The body 
of the script may be defined as such:

```groovy
import org.apereo.cas.api.*

def run(Object[] args) {
    def (passwordlessRequest,logger) = args
    
    logger.info("Locating user record for user $passwordlessRequest")
    
    /*
     ...
     Locate the record for the given username, 
     and return the result back to CAS.
     ...
    */
    
    def account = new PasswordlessUserAccount()
    account.setUsername(passwordlessRequest.username)
    account.setEmail("username@example.org")
    account.setName("TestUser")
    account.setPhone("123-456-7890") 
    account.setAttributes(Map.of("...", List.of("...", "...")) 
    account.setMultifactorAuthenticationEligible(TriStateBoolean.FALSE)  
    account.setRequestPassword(false)
    return account
}
```

{% include_cached casproperties.html properties="cas.authn.passwordless.accounts.groovy" %}

The following parameters are passed to the script:

| Parameter             | Description                                                                |
|-----------------------|----------------------------------------------------------------------------|
| `passwordlessRequest` | The `PasswordlessAuthenticationRequest` object that represents the user.   |
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)` |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
