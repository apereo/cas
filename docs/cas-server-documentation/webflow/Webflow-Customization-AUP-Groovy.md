---
layout: default
title: CAS - Web Flow Acceptable Usage Policy
category: Webflow Management
---

{% include variables.html %}

# Groovy Acceptable Usage Policy

Alternatively, CAS can be configured to use a Groovy script to verify status
of policies and store results. The script should match the following:

{% include {{ version }}/groovy-aup-configuration.md %}

```groovy
import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.util.*
import org.apereo.cas.aup.*
import org.springframework.webflow.execution.*

def verify(Object[] args) {
    def requestContext = args[0]
    def credential = args[1]
    def applicationContext = args[2]
    def principal = args[3]
    def logger = args[4]
    ...
    if (policyAccepted()) {
        return AcceptableUsagePolicyStatus.accepted(principal)
    }
    return AcceptableUsagePolicyStatus.denied(principal)
}

def submit(Object[] args) {
     def requestContext = args[0]
     def credential = args[1]
     def applicationContext = args[2]
     def principal = args[3]
     def logger = args[4]
     ...
     return true
}
     
/*
    A special callback function is implemented
    as an override to return an `AcceptableUsagePolicyTerms` 
    object back to CAS to be re-purposed
    for acceptable usage policy flows.
*/
def fetch(Object[] args) {
    def requestContext = args[0]
    def credential = args[1]
    def applicationContext = args[2]
    def principal = args[3]
    def logger = args[4]

    ...    

    return AcceptableUsagePolicyTerms.builder()
            .defaultText("Hello, World")
            .code(AcceptableUsagePolicyTerms.CODE)
            .build();
}
```

The parameters passed are as follows:

| Parameter             | Description
|-----------------------|-----------------------------------------------------------------------
| `requestContext`      | The object representing the Spring Webflow `RequestContext`.
| `credential`          | The object representing the authentication `Credential`.
| `applicationContext`  | The object representing the Spring `ApplicationContext`.
| `principal`           | The object representing the authenticated `Principal`.
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`.
