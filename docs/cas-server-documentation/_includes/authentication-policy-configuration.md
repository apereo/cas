Global authentication policy that is applied when CAS attempts to vend and validate tickets.

```properties
# cas.authn.policy.required-handler-authentication-policy-enabled=false
```

### Any

Satisfied if any handler succeeds. Supports a tryAll flag to avoid short circuiting
and try every handler even if one prior succeeded.

```properties
# cas.authn.policy.any.try-all=false
# cas.authn.policy.any.enabled=true
```

### All

Satisfied if and only if all given credentials are successfully authenticated.
Support for multiple credentials is new in CAS and this handler
would only be acceptable in a multi-factor authentication situation.

```properties
# cas.authn.policy.all.enabled=true
```

### Source Selection

Allows CAS to select authentication handlers based on the credential source. This allows the authentication engine to restrict the task of validating credentials
to the selected source or account repository, as opposed to every authentication handler.

```properties
# cas.authn.policy.source-selection-enabled=true
```

### Unique Principal

Satisfied if and only if the requesting principal has not already authenticated with CAS.
Otherwise the authentication event is blocked, preventing multiple logins.

<div class="alert alert-warning"><strong>Usage Warning</strong><p>Activating this policy is not without cost,
as CAS needs to query the ticket registry and all tickets present to determine whether the current user has established an authentication session anywhere. This will surely add a performance burden to the deployment. Use with care.</p></div>

```properties
# cas.authn.policy.unique-principal.enabled=true
```

### Not Prevented

Satisfied if and only if the authentication event is not blocked by a `PreventedException`.

```properties
# cas.authn.policy.not-prevented.enabled=true
```

### Required

Satisfied if and only if a specified handler successfully authenticates its credential.

```properties
# cas.authn.policy.req.try-all=false
# cas.authn.policy.req.handler-name=handlerName
# cas.authn.policy.req.enabled=true
```

### Groovy

Execute a groovy script to detect authentication policy.

```properties
# cas.authn.policy.groovy[0].script=file:/etc/cas/config/account.groovy
```

The script may be designed as:

```groovy
import java.util.*
import org.apereo.cas.authentication.exceptions.*
import javax.security.auth.login.*

def Exception run(final Object... args) {
    def principal = args[0]
    def logger = args[1]

    if (conditionYouMayDesign() == true) {
        return new AccountDisabledException()
    }
    return null;
}
```

### REST

Contact a REST endpoint via `POST` to detect authentication policy.
The message body contains the CAS authenticated principal that can be used
to examine account status and policy.

{% include {{ version }}/rest-integration.md configKey="cas.authn.policy.rest[0]" %}

Response codes from the REST endpoint are translated as such:

| Code                   | Result
|------------------------|---------------------------------------------
| `200`          | Successful authentication.
| `403`, `405`   | Produces a `AccountDisabledException`
| `404`          | Produces a `AccountNotFoundException`
| `423`          | Produces a `AccountLockedException`
| `412`          | Produces a `AccountExpiredException`
| `428`          | Produces a `AccountPasswordMustChangeException`
| Other          | Produces a `FailedLoginException`
