---
layout: default
title: CAS - MongoDb Authentication
category: Authentication
---
{% include variables.html %}


# MongoDb Authentication

Verify and authenticate credentials against a [MongoDb](https://www.mongodb.org/) instance.
Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-mongo" %}

{% include {{ version }}/principal-transformation-configuration.md configKey="cas.authn.mongo" %}

{% include {{ version }}/password-encoding-configuration.md configKey="cas.authn.mongo" %}

{% include casproperties.html properties="cas.authn.mongo" excludes="principal-transformation,password-encoder" %}

Accounts are expected to be found as such in collections:

```json
{
    "username": "casuser",
    "password": "34598dfkjdjk3487jfdkh874395",
    "first_name": "john",
    "last_name": "smith"
}
```
