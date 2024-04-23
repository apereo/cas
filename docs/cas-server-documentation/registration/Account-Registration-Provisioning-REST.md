---
layout: default
title: CAS - Account Registration Provisioning
category: Registration
---
                  
{% include variables.html %}

# Account (Self-Service) Registration - REST Provisioning

Account registration requests can be submitted to an external REST API via a `POST` that 
is responsible for managing and storing the account in the appropriate systems of record.

{% include_cached casproperties.html properties="cas.account-registration.provisioning.rest" %}

The expected response status code form the API call is `200`. The API request body will contain
the account registration request as such:

```json
{
  "@class":"org.apereo.cas.acct.AccountRegistrationRequest",
  "properties":{
    "@class":"java.util.LinkedHashMap",
    "username": "casuser",
    "field1": "value1"
  }
}
```
