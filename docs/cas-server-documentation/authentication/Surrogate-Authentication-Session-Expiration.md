---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}

# Session Expiration - Surrogate Authentication

An impersonation session can be assigned a specific expiration policy that would control how long a surrogate session
may last. This means that the SSO session established as part of impersonation will rightly vanish, once the
expiration policy dictates as such. It is recommended that you keep the expiration length short (i.e. 30 minutes) to avoid possible security issues.

<div class="alert alert-info">:information_source: <strong>Remember</strong><p>
The expiration policy assigned to impersonation sessions is expected to be <i>shorter</i> than the <i>normal</i> expiration policy
assigned to non-surrogate sessions. In other words, if the usual expiration policy that controls the single sign-on session is set to last
2 hours, the surrogate session expiration is expected to be a time period less than or equal to 2 hours.
</p></div>

{% include_cached casproperties.html properties="cas.authn.surrogate.tgt" %}
