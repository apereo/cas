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
    def username = args[0]
    def logger = args[1]
    
    logger.info("Locating user record for user $username")
    
    /*
     ...
     Locate the record for the given username, and return the result back to CAS.
     ...
    */
    
    def account = new PasswordlessUserAccount()
    account.setUsername(username)
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
