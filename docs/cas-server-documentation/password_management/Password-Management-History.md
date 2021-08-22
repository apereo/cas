---
layout: default
title: CAS - Password Management
category: Password Management
---

{% include variables.html %}

# Password History - Password Management

CAS allows for strategies to track and storage recycled password. Recycled 
passwords are kept in storage for the user account and are 
examined upon password updates for validity. 

{% include casproperties.html properties="cas.authn.pm.history.core" %}

Once password history functionality is enabled, passwords can be tracked 
in history via a Groovy or an in-memory backend. Specific storage 
options may also provide their own support for password history.
 
## Groovy

Password history tracking, once enabled, can be handed off to an external Groovy script as such:

```groovy
def exists(Object[] args) {
    def request = args[0]
    def logger = args[1]
    return false
}

def store(Object[] args) {
    def request = args[0]
    def logger = args[1]
    return true
}

def fetchAll(Object[] args) {
    def logger = args[0]
    return []
}

def fetch(Object[] args) {
    def username = args[0]
    def logger = args[1]
    return []
}   

def remove(Object[] args) { 
    def username = args[0]
    def logger = args[1]
}

def removeAll(Object[] args) { 
    def logger = args[0]
}
```

The `request` parameter encapsulates a `PasswordChangeRequest` object, carrying `username` and `password` fields.

{% include casproperties.html properties="cas.authn.pm.history.groovy" %}
  
## Custom

If you wish to create your own password hisory service, you will need to
design a component and register it with CAS as such:

```java
@Bean
public PasswordHistoryService passwordHistoryService() {
    return new CustomPasswordHistoryService();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.
