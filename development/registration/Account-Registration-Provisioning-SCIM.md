# Account (Self-Service) Registration - SCIM Provisioning

Source: https://apereo.github.io/cas/development/registration/Account-Registration-Provisioning-SCIM.html

The following settings and properties are available from the CAS configuration catalog:

- `cas.account-registration.provisioning.scim.enabled` (Optional; type `Boolean`; default `false`): Whether provisioning to SCIM targets should be enabled for delegated authentication attempts.

Provisioning tasks can be carried out using the CAS [SCIM Provisioning](https://apereo.github.io/cas/development/integration/SCIM-Provisioning.html). Once enabled and configured, account registration requests may be provisioned via SCIM to other systems.

> **Usage**
>
> SCIM integration support for delegated authentication is only handled via SCIM `v2`.
