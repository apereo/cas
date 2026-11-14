# Required - Authentication Policy

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Policy-Required.html

Satisfied if and only if a specified handler successfully authenticates its credential.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.policy.req.handler-name` (Required; type `String`; default `handlerName`): The handler name which must have successfully executed and validated credentials.
- `cas.authn.policy.required-attributes.attributes` (Required; type `Map<String,String>`): Map of attributes that are required to found either as authentication attributes or principal attributes.
- `cas.authn.policy.req.enabled` (Optional; type `Boolean`; default `false`): Enables the policy.
- `cas.authn.policy.req.name` (Optional; type `String`): The name of the authentication policy.
- `cas.authn.policy.req.order` (Optional; type `Integer`): The execution order of this policy.
- `cas.authn.policy.req.try-all` (Optional; type `Boolean`; default `false`): Ensure number of provided credentials does not match the sum of authentication successes and failures.
- `cas.authn.policy.required-attributes.enabled` (Optional; type `Boolean`; default `false`): Enables the policy.
- `cas.authn.policy.required-attributes.name` (Optional; type `String`): The name of the authentication policy.
- `cas.authn.policy.required-attributes.order` (Optional; type `Integer`): The execution order of this policy.
- `cas.authn.policy.required-handler-authentication-policy-enabled` (Optional; type `Boolean`; default `false`): Global authentication policy that is applied when CAS attempts to vend and validate tickets. Checks to make sure a particular authentication handler has successfully executed and validated credentials. Required handlers are defined per registered service.
