# Session Expiration - Surrogate Authentication

Source: https://apereo.github.io/cas/development/authentication/Surrogate-Authentication-Session-Expiration.html

An impersonation session can be assigned a specific expiration policy that would control how long a surrogate session may last. This means that the SSO session established as part of impersonation will rightly vanish, once the expiration policy dictates as such. It is recommended that you keep the expiration length short (i.e. 30 minutes) to avoid possible security issues.

> **Remember**
>
> The expiration policy assigned to impersonation sessions is expected to be *shorter* than the *normal* expiration policy assigned to non-surrogate sessions. In other words, if the usual expiration policy that controls the single sign-on session is set to last 2 hours, the surrogate session expiration is expected to be a time period less than or equal to 2 hours.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.surrogate.tgt.time-to-kill-in-seconds` (Optional; type `Long`; default `1800`): Timeout in seconds to kill the surrogate session and consider tickets expired.
