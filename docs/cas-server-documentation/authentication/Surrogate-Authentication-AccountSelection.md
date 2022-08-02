---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}

# Account Selection - Surrogate Authentication

The surrogate user selection can happen via the following ways.

## Preselected

This is the case where the surrogate user identity is known 
beforehand and is provided to CAS upon login using a special syntax.
When entering credentials, the following syntax should be used:

```bash
[surrogate-userid][separator][primary-userid]
```

For example, if you are `casuser` and you need to switch to `jsmith` as the 
surrogate user, the credential id provided to CAS would be `jsmith+casuser` where 
the separator is `+` and can be altered via the CAS configuration. You will 
need to provide your own password of course.

## GUI

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
    
## Wildcard

The underlying account store can mark a primary *impersonator* account with special permissions
and privileges to allow it to impersonate any other account. Accounts that are whitelisted and wildcarded
in this strategy are not assigned a specific list of authorized impersonatees but instead are able to impersonate
any other username without any restrictions or additional checks. 

<div class="alert alert-warning"><strong>Usage Warning</strong>
<p>Be careful with this strategy! Designating an account as a wildcard will disable any and all other checks
on the surrgate/impersonatee account and CAS will completely back away from validating and verifying
the requested surrogate account.</p></div>

To designate an account as a wildcard, the account store must be able to return and provide a list of 
authorized *impersonatee* accounts for the primary user with `*` as the only entry in the list, indicating the primary
user is authorized to impersonate anyone. Also, note that account selection using a GUI approach is 
disabled and turned off for wildcarded accounts. The primary user tagged as a wildcard must begin the impersonation 
flow using the *Preselected* approach with a known surrogate user.
