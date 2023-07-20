---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - AWS Verified Permissions

[Amazon Verified Permissions](https://aws.amazon.com/verified-permissions/) is a scalable permissions 
management and fine-grained authorization service for the applications that you build.

This access strategy builds an authorization request and submits it to Amazon Verified Permissions. The specifics
of the authorization request are taught to CAS using the settings typically defined within the access strategy itself:

```json
{
  "@class": "org.apereo.cas.aws.authz.AmazonVerifiedPermissionsRegisteredServiceAccessStrategy",
  "credentialAccessKey": "...",
  "credentialSecretKey": "...",
  "region": "us-east-1",
  "policyStoreId": "...",
  "actionId": "read",
  "context": {
    "@class": "java.util.LinkedHashMap",
    "key": "value"
  }
}
```

The following fields are available to this access strategy:

| Field                 | Purpose                                                                       |
|-----------------------|-------------------------------------------------------------------------------|
| `credentialAccessKey` | <sup>[1]</sup> (Optional) The access key used to authenticate the request.    |
| `credentialSecretKey` | <sup>[1]</sup> (Optional) The secret key used to authenticate the request.    |
| `region`              | <sup>[1]</sup> (Optional) AWS region to use for authorization API requests.   |
| `policyStoreId`       | <sup>[1]</sup> (Optional) Policies in this policy store to use authorizations |
| `context`             | (Optional) Additional context used for granular authorization decisions.      |
| `actionId`            | <sup>[1]</sup> Specifies the requested action to be authorized.               |

<sub><i>[1] This field supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.</i></sub>
