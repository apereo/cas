# All - Authentication Policy

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Policy-All.html

Satisfied if and only if all given credentials are successfully authenticated. Support for multiple credentials is new in CAS and this handler would only be acceptable in a multi-factor authentication situation.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.policy.all-handlers.enabled` (Optional; type `Boolean`; default `false`): Enables the policy.
- `cas.authn.policy.all-handlers.name` (Optional; type `String`): The name of the authentication policy.
- `cas.authn.policy.all-handlers.order` (Optional; type `Integer`): The execution order of this policy.
- `cas.authn.policy.all.enabled` (Optional; type `Boolean`; default `false`): Enables the policy.
- `cas.authn.policy.all.name` (Optional; type `String`): The name of the authentication policy.
- `cas.authn.policy.all.order` (Optional; type `Integer`): The execution order of this policy.
