# Required Attributes - Authentication Policy

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Policy-Required-Attributes.html

Satisfied if and only if specified attributes can be found for the authentication attempt or the authenticated principal.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.policy.required-attributes.attributes` (Required; type `Map<String,String>`): Map of attributes that are required to found either as authentication attributes or principal attributes.
- `cas.authn.policy.required-attributes.enabled` (Optional; type `Boolean`; default `false`): Enables the policy.
- `cas.authn.policy.required-attributes.name` (Optional; type `String`): The name of the authentication policy.
- `cas.authn.policy.required-attributes.order` (Optional; type `Integer`): The execution order of this policy.
