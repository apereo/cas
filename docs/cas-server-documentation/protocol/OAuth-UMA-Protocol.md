---
layout: default
title: CAS - OAuth User-Managed Access Protocol
---

# User-Managed Access Protocol

User-Managed Access (UMA) is a lightweight access control protocol that defines a centralized workflow to allow an entity (user or corporation) 
to manage access to their resources.

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-oauth-uma</artifactId>
  <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties for this feature, please [review this guide](Configuration-Properties.html#oauth2-uma).

## Endpoints

### Requesting Party Token

Issue a `GET` request to `/oauth2.0/umaJwks` to retrieve signing keys.

### Policies

#### Create

Issue a `POST` request to `/oauth2.0/${resourceId}/policy` with the payload body as:

```json
{
  "permissions": [{
    "subject": "casuser",
    "scopes": ["read","write"],
    "claims": {
        "givenName": "CAS"
      }
    }]
}
```

#### Delete

Issue a `DELETE` request as `/oauth2.0/${resourceId}/policy/${policyId}`

#### Update

Issue a `PUT` request as `/oauth2.0/${resourceId}/policy/${policyId}` with the payload body as one matching the `POST` method.

#### Find

Issue a `GET` request as `/oauth2.0/${resourceId}/policy/` to fetch all policy definitions for a resource.
Issue a `GET` request as `/oauth2.0/${resourceId}/policy/${policyId}` to a specific policy definition for a resource.

### Resources

Resource-related operations are handled at endpoint `/oauth2.0/resourceSet`.

#### Create

The expected `POST` payload body is:

```json
{
  "uri": "...",
  "type": "...",
  "name": "...",
  "icon_uri": "...",
  "resource_scopes": ["read","write"]
}
```

#### Delete

Issue a `DELETE` request as `${resourceSetEndpoint}/${resourceId}`

#### Update

Issue a `PUT` request as `${resourceSetEndpoint}/${resourceId}` with the payload body as one matching the `POST` method.

#### Find

Issue a `GET` request as `${resourceSetEndpoint}/${resourceId}` to fetch a specific resource definition. 
Issue a `GET` request as `${resourceSetEndpoint}` to fetch all resource definitions.
