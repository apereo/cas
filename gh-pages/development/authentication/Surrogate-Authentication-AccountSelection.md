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
ou may want to switch, the credential id provided to CAS would be `+casuser` where 
the separator is `+` and can be altered via the CAS configuration. You 
will need to provide your own password of course.
