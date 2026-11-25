# Passwordless Authentication - User Selection Menu

Source: https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-UserSelectionMenu.html

A passwordless account can be decorated to allow the user to select from a menu of available authentication options. This feature is useful in scenarios where the account may be eligible for multiple forms of authentication and the user is allowed to choose the most appropriate one. The passwordless account store is ultimately responsible for determining the list of available authentication options and whether the user does quality for the authentication selection flow.

As an example, the following account managed via the [JSON passwordless account storage](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Storage-JSON.html) instructs CAS using the `allowSelectionMenu` flag to allow the flow to present available forms of authentication to the user:

```json
{
  "@class" : "java.util.LinkedHashMap",
  "casuser" : {
    "@class" : "org.apereo.cas.api.PasswordlessUserAccount",
    "username": "casuser",
    "name" : "CAS",
    "email": "casuser@example.org",
    "allowSelectionMenu": true,
    "requestPassword": true,
    "delegatedAuthenticationEligible": "TRUE",
    "allowedDelegatedClients" : [ "java.util.ArrayList", [ "ExternalIdP" ] ],
    "multifactorAuthenticationEligible": "TRUE",
    "attributes" : {
      "@class" : "java.util.TreeMap",
      "memberOf" : [ "java.util.ArrayList", [ "mfa" ] ],
      "email": [ "java.util.ArrayList", [ "casuser@example.org" ] ]
    }
  }
}
```

The above account allows CAS to choose between the following options:

- Authentication via password.
- [Delegated authentication](https://apereo.github.io/cas/development/integration/Delegate-Authentication.html) via the `ExternalIdP` identity provider.
- [Multifactor authentication](https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication.html).
- Passwordless [token-based authentication](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication.html).
- [Passkeys](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Passkeys.html), when WebAuthn is allowed to act as a primary authentication strategy.
