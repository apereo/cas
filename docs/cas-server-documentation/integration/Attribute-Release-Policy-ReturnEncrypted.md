---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Return Encrypted

Encrypt and encode all all allowed attributes in base-64 using the assigned registered service public key.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnEncryptedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ]
  },
  "publicKey" : {
    "@class" : "org.apereo.cas.services.RegisteredServicePublicKeyImpl",
    "location" : "classpath:public.key",
    "algorithm" : "RSA"
  }
}
```

The keys can be generated via the following commands:

```bash
openssl genrsa -out private.key 1024
openssl rsa -pubout -in private.key -out public.key -inform PEM -outform DER
openssl pkcs8 -topk8 -inform PER -outform DER -nocrypt -in private.key -out private.p8
```

Attributes authorized and allowed for release by this policy may not necessarily be available
as resolved principal attributes and can be resolved on the fly dynamically
using the [attribute definition store](Attribute-Definitions.html).

