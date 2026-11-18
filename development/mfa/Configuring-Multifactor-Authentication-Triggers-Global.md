# Global Multifactor Authentication Trigger

Source: https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication-Triggers-Global.html

MFA can be triggered for all applications and users regardless of individual settings. In other words, you may only enable this trigger if you wish to activate a multifactor provider for all requests, applications and users.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.triggers.global.global-provider-id` (Optional; type `String`): MFA can be triggered for all applications and users regardless of individual settings. This setting holds the value of an MFA provider that shall be activated for all requests, regardless. Multiple provider identifiers can be specified here via a comma-separated syntax which may force CAS to launch into a provider selection and resolution flow.
