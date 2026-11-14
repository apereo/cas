# Not Prevented - Authentication Policy

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Policy-NotPrevented.html

Satisfied if and only if the authentication event is not blocked by a `PreventedException`.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.policy.not-prevented.enabled` (Optional; type `Boolean`; default `false`): Enables the policy.
- `cas.authn.policy.not-prevented.name` (Optional; type `String`): The name of the authentication policy.
- `cas.authn.policy.not-prevented.order` (Optional; type `Integer`): The execution order of this policy.
