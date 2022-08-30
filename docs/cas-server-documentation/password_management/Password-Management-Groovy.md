---
layout: default
title: CAS - Password Management
category: Password Management
---

{% include variables.html %}

# Password Management - Groovy

Accounts and password may be determined and handled using a customized Groovy script. The outline of the script may match the following:

```groovy
import org.apereo.cas.pm.*

def change(Object[] args) {
    def credential = args[0]
    def passwordChangeBean = args[1]
    def logger = args[2]
    return true
}

def findEmail(Object[] args) {
    def username = (args[0] as PasswordManagementQuery).username
    def logger = args[1]
    return "cas@example.org"
}

def findPhone(Object[] args) {
    def username = (args[0] as PasswordManagementQuery).username
    def logger = args[1]
    return "1234567890"
}

def findUsername(Object[] args) {
    def email = (args[0] as PasswordManagementQuery).email
    def logger = args[1]
    return "casuser"
}

def getSecurityQuestions(Object[] args) {
    def username = (args[0] as PasswordManagementQuery).username
    def logger = args[1]
    return [securityQuestion1: "securityAnswer1"]
}

def updateSecurityQuestions(Object[] args) {
    def query = args[0] as PasswordManagementQuery
    def logger = args[1]
    // Execute update...
}

def unlockAccount(Object[] args) {
    def query = args[0] as Credential
    def logger = args[1]
    // Execute unlock...
    return true
}
```

{% include_cached casproperties.html properties="cas.authn.pm.groovy" %}
