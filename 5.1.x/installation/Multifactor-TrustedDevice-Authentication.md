---
layout: default
title: CAS - Trusted Device Multifactor Authentication
---

# Multifactor Authentication Trusted Device/Browser

In addition to triggers that are provided by the [MFA functionality](Configuring-Multifactor-Authentication.html) of CAS, there may be
cases where you wish to let the user decide if the current browser/device should be trusted so as to skip subsequent MFA requests. The
objective is for CAS to remember that decision for a configurable period of time and not bother the user with MFA until the decision
is either forcefully revoked or considered expired.

Trusting a device during an MFA workflow would mean that the ultimate decision is remembered for that **user** of that **location**
of that **device**. These keys are combined together securely and assigned to the final decision.

Before deployment, you should consider the following:

- Should users be optionally allowed to authorize the "current" device?
- ...or must that happen automatically once MFA is commenced?
- How should user decisions and choices be remembered? Where are they stored?
- How long should user decisions be trusted by CAS?
- How is a trusted authentication session communicated back to an application?

Note that enabling this feature by default means it's globally applied to all in the case if you have multiple MFA providers turned on.
This can be optionally disabled and applied only to a selected set of providers.

## Configuration

Support is provided via the following module:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-trusted-mfa</artifactId>
    <version>${cas.version}</version>
</dependency>
```

## Settings

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#multifactor-trusted-devicebrowser).

## Authentication Context

If an MFA request is bypassed due to a trusted authentication decision, applications will receive a special attribute as part of
the validation payload that indicates this behavior. Applications must further account for the scenario where they ask for an MFA
mode and yet don't receive confirmation of it in the response given the authentication session was trusted and MFA bypassed.

## Storage

User decisions must be remembered and processed later on subsequent requests.

### Default

If you do nothing, by default records are kept inside the runtime memory and cached for a configurable amount of time.
This is most useful if you have a very small deployment with a small user base or if you simply wish to demo the functionality.

### JDBC

User decisions may also be kept inside a regular RDBMS of your own choosing.

Support is provided via the following module:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-trusted-mfa-jdbc</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To learn how to configure database drivers, [please see this guide](JDBC-Drivers.html).
To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#jdbc-storage).


### Mongo

User decisions may also be kept inside a MongoDb instance.

Support is provided via the following module:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-trusted-mfa-mongo</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#mongodb-storage).


### REST

If you wish to completely delegate the management, verification and persistence of user decisions, you may design a REST API
which CAS shall contact to verify user decisions and remember those for later.

Support is provided via the following module:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-trusted-mfa-rest</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#rest-storage).

#### Retrieve Trusted Records

A `GET` request that returns all trusted authentication records that are valid and not-expired.

```bash
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET ${endpointUrl}/[principal]
```

Response payload may produce a collection of objects that contain:

```json
[
    {
      "principal": "casuser",
      "geography": "...",
      "date": "YYYY-MM-dd",
      "name": "Office",
      "key": "..."
    }
]
```

#### Store Trusted Records

A `POST` request that stores a newly trusted device record.

```bash
curl -H "Content-Type: application/json" -X POST -d '${json}' ${endpointUrl}
```

`POST` data will match the following block:

```json
{
    "principal": "...",
    "geography": "...",
    "date": "...",
    "name": "...",
    "key": "..."
}
```

Response payload shall produce a `200` http status code to indicate a successful operation.
