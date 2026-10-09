---
layout: default
title: CAS - Password Management - Groovy
category: Password Management
---

{% include variables.html %}

# Password Management - Groovy

Look up accounts, change passwords and handle security questions with a Groovy script.

Accounts and password may be determined and handled using a customized Groovy script. The outline of the script may match the following:

```groovy
import org.apereo.cas.pm.*

def change(Object[] args) {
    def (passwordChangeBean,logger) = args
    return true
}

def findEmails(Object[] args) {
    def (passwordMgmtQuery,logger) = args
    return "cas@example.org"
}

def findPhone(Object[] args) {
    def (passwordMgmtQuery,logger) = args
    return "1234567890"
}

def findUsername(Object[] args) {
    def (passwordMgmtQuery,logger) = args
    return "casuser"
}

def getSecurityQuestions(Object[] args) {
    def (passwordMgmtQuery,logger) = args
    return [securityQuestion1: "securityAnswer1"]
}

def updateSecurityQuestions(Object[] args) {
    def (passwordMgmtQuery,logger) = args
    // Execute update...
}

def unlockAccount(Object[] args) {
    def (credential,logger) = args
    // Execute unlock...
    return true
}
```

{% include_cached casproperties.html properties="cas.authn.pm.groovy" %}

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
