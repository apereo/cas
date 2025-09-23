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

{% include_cached casproperties.html properties="cas.authn.pm.history.core" %}

Once password history functionality is enabled, passwords can be tracked 
in history via a Groovy or an in-memory backend. Specific storage 
options may also provide their own support for password history.
 
{% tabs passwordhistory %}

{% tab passwordhistory <i class="fa fa-file-code px-1"></i>Groovy %}

Password history tracking, once enabled, can be handed off to an external Groovy script as such:

```groovy
def exists(Object[] args) {
    def (request,logger) = args
    return false
}

def store(Object[] args) {
    def (request,logger) = args
    return true
}

def fetchAll(Object[] args) {
    def (logger) = args
    return []
}

def fetch(Object[] args) {
    def (username,logger) = args
    return []
}   

def remove(Object[] args) { 
    def (username,logger) = args
}

def removeAll(Object[] args) { 
    def (logger) = args
}
```

The `request` parameter encapsulates a `PasswordChangeRequest` object, carrying `username` and `password` fields.

{% include_cached casproperties.html properties="cas.authn.pm.history.groovy" %}

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% tab passwordhistory Custom %}

If you wish to create your own password history service, you will need to
design a component and register it with CAS as such:

```java
@Bean
public PasswordHistoryService passwordHistoryService() {
    return new CustomPasswordHistoryService();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about
how to register configurations into the CAS runtime.

{% endtab %}

{% endtabs %}

