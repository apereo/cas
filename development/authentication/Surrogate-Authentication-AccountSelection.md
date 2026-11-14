# Account Selection - Surrogate Authentication

Source: https://apereo.github.io/cas/development/authentication/Surrogate-Authentication-AccountSelection.html

The surrogate user selection can happen via the following ways.

**Preselected**

This is the case where the surrogate user identity is known beforehand and is provided to CAS upon login using a special syntax. When entering credentials, the following syntax should be used:

```bash
[impersonated-userid][separator][primary-userid]
```

For example, if you are `casuser` and you need to switch to `jsmith` as the surrogate (impersonated) user, the credential id or username provided to CAS would be `jsmith+casuser` where the separator is `+` and can be altered via the CAS configuration. CAS will first authenticate `casuser` as the primary user, and will then switch to `jsmith` when allowed.

**GUI**

This is the case where the surrogate user identity is *not* known beforehand, and you wish to choose the account from a pre-populated list. When entering credentials, the following syntax should be used:

```bash
[separator][primary-userid]
```

For example, if you are `casuser` and you need to locate the surrogate account to which you may want to switch, the credential id provided to CAS would be `+casuser` where the separator is `+` and can be altered via the CAS configuration. You will need to provide your own password of course.

**Principal Attribute**

This option is similar to the *Wildcard* strategy, except that the impersonation logic is based off of a predefined principal attribute that is resolved and whose value(s) matches a certain regular expression pattern. That is, CAS can be configured to look for a specific attribute name for the primary/admin user and ensure that the attribute value matches a pattern to allow impersonation. The attribute is expected to be already be found and resolved for the admin user.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.surrogate.core.principal-attribute-names` (Optional; type `List<String>`): Impersonation can be authorized for all primary users/subjects carrying specific attributes with a predefined matching value specified via `#principalAttributeValues`. Needless to say, the attributes need to have been resolved for the primary principal prior to this step. Matching and comparison operations are case insensitive.
- `cas.authn.surrogate.core.principal-attribute-values` (Optional; type `List<String>`): The regular expression that is cross matched against the principal attribute to determine if the account is authorized for impersonation. Matching and comparison operations are case insensitive.
- `cas.authn.surrogate.core.separator` (Optional; type `String`; default `+`): The separator character used to distinguish between the surrogate account and the admin/primary account. For example, if you are `casuser` and you need to switch to `jsmith` as the surrogate (impersonated) user, the username provided to CAS would be `jsmith+casuser`.

> **Usage Warning**
>
> Be careful with this strategy! Once a match is found, CAS will completely back away from validating and verifying the requested surrogate account and the admin user is authorized to impersonate everyone and anyone.

**Wildcard**

The underlying account store can mark a primary *impersonator* account with special permissions and privileges to allow it to impersonate any other account. Accounts that are whitelisted and wildcarded in this strategy are not assigned a specific list of authorized impersonatees but instead are able to impersonate any other username without any restrictions or additional checks.

> **Usage Warning**
>
> Be careful with this strategy! Designating an account as a wildcard will disable any and all other checks on the surrogate/impersonated account and CAS will completely back away from validating and verifying the requested surrogate account.

To designate an account as a wildcard, the account store must be able to return and provide a list of authorized *impersonated* accounts for the primary user with `*` as the only entry in the list, indicating the primary user is authorized to impersonate anyone. Also, note that account selection using a GUI approach is disabled and turned off for wildcarded accounts. The primary user tagged as a wildcard must begin the impersonation flow using the *Preselected* approach with a known surrogate user.
