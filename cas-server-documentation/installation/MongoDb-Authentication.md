---
layout: default
title: CAS - MongoDb Authentication
---

# MongoDb Authentication
Verify and authenticate credentials against a [MongoDb](https://www.mongodb.org/) instance.

```properties
#CAS components mappings
primaryAuthenticationHandler=mongoAuthenticationHandler
```

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-mongo</artifactId>
  <version>${cas.version}</version>
</dependency>
```

The following settings are applicable:

```properties
cas.authn.mongo.collection.name=users
cas.authn.mongo.db.host=mongodb://user:password@ds061954.somewhere.com:61954/database
cas.authn.mongo.attributes=attribute1,attribute2
cas.authn.mongo.username.attribute=username
cas.authn.mongo.password.attribute=password
```
