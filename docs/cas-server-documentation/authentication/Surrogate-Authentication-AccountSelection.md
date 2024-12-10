---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}

# Account Selection - Surrogate Authentication

The surrogate user selection can happen via the following ways.

{% tabs impersonationoptions %}

{% tab impersonationoptions <i class="fa fa-filter px-1"></i> Preselected %}

This is the case where the surrogate user identity is known
beforehand and is provided to CAS upon login using a special syntax.
When entering credentials, the following syntax should be used:

```bash
[impersonated-userid][separator][primary-userid]
```

For example, if you are `casuser` and you need to switch to `jsmith` as the
surrogate (impersonated) user, the credential id or username provided to CAS would be `jsmith+casuser` where
the separator is `+` and can be altered via the CAS configuration. CAS will first authenticate `casuser` as the
primary user, and will then switch to `jsmith` when allowed.

{% endtab %}

{% tab impersonationoptions <i class="fa fa-masks-theater px-1"></i> GUI %}

This is the case where the surrogate user identity is *not* known
beforehand, and you wish to choose the account from a pre-populated
list. When entering credentials, the following syntax should be used:

```bash
[separator][primary-userid]
```

For example, if you are `casuser` and you need to locate the surrogate account to which
you may want to switch, the credential id provided to CAS would be `+casuser` where
the separator is `+` and can be altered via the CAS configuration. You
will need to provide your own password of course.

{% endtab %}

{% tab impersonationoptions <i class="fa fa-person px-1"></i>Principal Attribute %}

This option is similar to the *Wildcard* strategy, except that the impersonation logic is based off of a predefined principal attribute
that is resolved and whose value(s) matches a certain regular expression pattern. That is, CAS can be configured to look for a specific attribute
name for the primary/admin user and ensure that the attribute value matches a pattern to allow impersonation. The attribute is expected 
to be already be found and resolved for the admin user.

{% include_cached casproperties.html properties="cas.authn.surrogate.core" %}

<div class="alert alert-warning">:warning: <strong>Usage Warning</strong>
<p>Be careful with this strategy! Once a match is found, CAS will completely back away from validating and verifying
the requested surrogate account and the admin user is authorized to impersonate everyone and anyone.</p></div>

{% endtab %}

{% tab impersonationoptions <i class="fa fa-sun px-1"></i> Wildcard %}

The underlying account store can mark a primary *impersonator* account with special permissions
and privileges to allow it to impersonate any other account. Accounts that are whitelisted and wildcarded
in this strategy are not assigned a specific list of authorized impersonatees but instead are able to impersonate
any other username without any restrictions or additional checks.

<div class="alert alert-warning">:warning: <strong>Usage Warning</strong>
<p>Be careful with this strategy! Designating an account as a wildcard will disable any and all other checks
on the surrogate/impersonated account and CAS will completely back away from validating and verifying
the requested surrogate account.</p></div>

To designate an account as a wildcard, the account store must be able to return and provide a list of
authorized *impersonated* accounts for the primary user with `*` as the only entry in the list, indicating the primary
user is authorized to impersonate anyone. Also, note that account selection using a GUI approach is
disabled and turned off for wildcarded accounts. The primary user tagged as a wildcard must begin the impersonation
flow using the *Preselected* approach with a known surrogate user.

{% endtab %}

{% endtabs %}
