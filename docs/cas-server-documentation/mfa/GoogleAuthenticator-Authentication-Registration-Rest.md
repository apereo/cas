---
layout: default
title: CAS - Google Authenticator Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# REST Google Authenticator Registration

Registration records may also be passed along to a REST endpoint.
The behavior is only activated when an endpoint url is provided.

| Method    | Headers             | Expected Response     | Behavior
|-----------|------------------------------------------------------------------------------------
| `GET`     | `username`          | `200`. Account records in the body for the user. | Fetch user records
| `GET`     | `id`                | `200`. Account record in the body for the user. | Fetch record for the given identifier.
| `GET`     | `id`, `username`    | `200`. Account record in the body for the user. | Fetch user record for the given identifier.
| `GET`     | N/A                 | `200`. Account records currently registered. | Fetch all records
| `DELETE`  | N/A                 | `200`. | Delete all records.
| `DELETE`  | `username`          | `200`. Count deleted records. | Delete all records assigned to user
| `POST`    | `username`, `validationCode`, `secretKey`, `scratchCodes`, `name` | `200`. `true/false` in the body. | Save user record

A sample payload that lists device registration records for the user might be:

```json 
[
    "java.util.ArrayList", [{
        "@class": "org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount",
        "scratchCodes": ["java.util.ArrayList", [14883628,81852839,40126334,86724930,54355266] ],
        "id": 123456,
        "secretKey": "UM6ALPJU34CBNFTBBLRFMKBNANMFAIBW",
        "validationCode": 565889,
        "username": "casuser",
        "name": "required-account-name",
        "registrationDate": "2018-06-20T09:47:31.761155Z"
    }]
]
```

The following endpoints need also be available:

| Method    | Endpoint   | Headers           | Expected Response     | Behavior
|-----------|------------------------------------------------------------------------------------
| `GET`     | `count`    | N/A             | `200`. Numeric count | Count all records
| `GET`     | `count`    | `username`             | `200`. Numeric count | Count all records for the user

{% include {{ version }}/rest-gauth-configuration.md %}

### JSON

Registration records may also be kept inside a single JSON file for all users.
The behavior is only activated when a path to a JSON data store file is provided,
and otherwise CAS may fallback to keeping records in memory. This feature is mostly
useful during development and for demo purposes.

{% include {{ version }}/json-gauth-configuration.md %}

## REST Protocol Credential Extraction 

In the event that the [CAS REST Protocol](../protocol/REST-Protocol.html) is turned on, a special credential extractor is injected into the REST authentication engine in order to recognize credentials and authenticate them as part of the REST request. 
The expected parameter name in the request body is `gauthotp`. The account identifier may also be passed using the `gauthacct` parameter in the request body.
