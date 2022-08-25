---
layout: default
title: CAS - Releasing Principal Id
category: Attributes
---

{% include variables.html %}

# Encrypted Principal Id 

Most if not all username attribute providers are able to encrypt the resolved username, 
assuming the service definition is given a public key.

The key can be generated via the following commands:

```bash
openssl genrsa -out private.key 1024
openssl rsa -pubout -in private.key -out public.key -inform PEM -outform DER
openssl pkcs8 -topk8 -inform PER -outform DER -nocrypt -in private.key -out private.p8
```

The public key is then configured for a service definition in CAS:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider",
    "encryptUsername" : "true"
  },
  "publicKey" : {
    "@class" : "org.apereo.cas.services.RegisteredServicePublicKeyImpl",
    "location" : "classpath:public.key",
    "algorithm" : "RSA"
  }
}
``` 

The configuration of the public key component qualifies to use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.

The application can then proceed to decrypt the username using its own private key.
The following sample code demonstrates how that might be done in Java:

```java
var casUsername = ...
var privateKey = ...
var cipher = Cipher.getInstance(privateKey.getAlgorithm());
var cred64 = decodeBase64(encodedPsw);
cipher.init(Cipher.DECRYPT_MODE, privateKey);
var cipherData = cipher.doFinal(casUsername);
return new String(cipherData);
```
