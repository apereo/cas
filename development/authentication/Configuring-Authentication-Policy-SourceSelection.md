# Source Selection - Authentication Policy

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Policy-SourceSelection.html

Allows CAS to select authentication handlers based on the credential source. This allows the authentication engine to restrict the task of validating credentials to the selected source or account repository, as opposed to every authentication handler.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.policy.source-selection-enabled` (Optional; type `Boolean`; default `false`): If true, allows CAS to select authentication handlers based on the credential source. This allows the authentication engine to restrict the task of validating credentials to the selected source or account repository, as opposed to every authentication handler registered with CAS at runtime.
