# Unique Principal - Authentication Policy

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Policy-UniquePrincipal.html

Satisfied if and only if the requesting principal has not already authenticated with CAS. Otherwise the authentication event is blocked, preventing multiple logins.

> **Usage Warning**
>
> Activating this policy is not without cost, as CAS needs to query the ticket registry and all tickets present to determine whether the current user has established an authentication session anywhere. This will surely add a performance burden to the deployment. Use with care.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.policy.unique-principal.enabled` (Optional; type `Boolean`; default `false`): Enables the policy.
- `cas.authn.policy.unique-principal.maximum-allowed-sessions` (Optional; type `Integer`; default `0`): Total number of allowed sessions that users can use simultaneously. Default is `0`, which means user can only have one session.
- `cas.authn.policy.unique-principal.name` (Optional; type `String`): The name of the authentication policy.
- `cas.authn.policy.unique-principal.order` (Optional; type `Integer`): The execution order of this policy.
