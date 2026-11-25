# Any - Authentication Policy

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Policy-Any.html

Satisfied if any handler succeeds. Supports a `tryAll` flag to avoid short circuiting and try every handler even if one prior succeeded.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.policy.any.enabled` (Optional; type `Boolean`; default `false`): Enables the policy.
- `cas.authn.policy.any.name` (Optional; type `String`): The name of the authentication policy.
- `cas.authn.policy.any.order` (Optional; type `Integer`): The execution order of this policy.
- `cas.authn.policy.any.try-all` (Optional; type `Boolean`; default `false`): Avoid short circuiting and try every handler even if one prior succeeded. Ensure number of provided credentials does not match the sum of authentication successes and failures
