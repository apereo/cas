---
layout: default
title: CAS - Passwordless Authentication
---

# Passwordless Authentication

Passwordless authentication is a form of authentication in CAS where passwords take the form of one-time tokens that expire after a configurable period of time. 
Using this strategy, users are simply asked for a username typically which is used to locate the user record to identify forms of contact such as email and phone
number. One located, the generated token is sent to the user via the configured notification strategies (i.e. email, sms, etc) where the user is then expected to 
provide the token back to CAS in order to proceed. 

In order to successfully implement this feature, configuration needs to be in place to contact account stores that hold user records who qualify for passwordless authentication.
Similarly, CAS must be configured to manage issues tokens in order to execute find, validate, expire or save operations in appropriate data stores.

## Overview

Support is enabled by including the following module in the overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-passwordless</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#passwordless-authentication).

## Account Stores

User records that qualify for passwordless authentication must be found by CAS using one of the following strategies. All strategies may be configured
using CAS settings and are activated depending on the presence of configuration values.

### Simple

This strategy provides a static map of usernames that are linked to their method of contact, such as email or phone number. It is best used
for testing and demo purposes. The key in the map is taken to be the username eligible for authentication while the value can either be an email
address or phone number that would be used to contact the user with issued tokens.

### Groovy

This strategy allows one to locate user records via a Groovy script. The body of the script may be defined as such:

```groovy
import org.apereo.cas.api.*

def run(Object[] args) {
    def username = args[0]
    def logger = args[1]
    logger.info("Locating user record for user $username")
    
    def account = new PasswordlessUserAccount()
    account.setUsername(username)
    account.setEmail("username@example.org")
    account.setName("TestUser")
    account.setPhone("123-456-7890")
    return account
}
```

### REST

This strategy allows one design REST endpoints in charge of locating passwordless user records. A successful execution of the endpoint  
would produce a response body similar to the following:

```json
{
  "@class" : "org.apereo.cas.api.PasswordlessUserAccount",
  "username" : "casuser",
  "email" : "cas@example.org",
  "phone" : "123-456-7890",
  "name" : "CASUser"
}
```

## Token Management


