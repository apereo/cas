# Groovy - Authentication Policy

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Policy-Groovy.html

Decide whether an authentication attempt succeeds with a Groovy script that inspects the authentication result and can reject it with an exception.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.policy.groovy[0].script` (Required; type `String`): Path to the groovy script to execute.
- `cas.authn.policy.groovy` (Optional; type `List<GroovyAuthenticationPolicyProperties>`): Execute a groovy script to detect authentication policy.
- `cas.authn.policy.groovy[0].enabled` (Optional; type `boolean`): Enables the policy.
- `cas.authn.policy.groovy[0].name` (Optional; type `String`): The name of the authentication policy.
- `cas.authn.policy.groovy[0].order` (Optional; type `int`): The execution order of this policy.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

The script may be designed as:

```groovy
import java.util.*
import org.apereo.cas.authentication.exceptions.*
import javax.security.auth.login.*

def run(Object... args) {
    def (authentication, context, applicationContext, logger) = args

    if (conditionYouMayDesign() == true) {
        return new AccountDisabledException()
    }
    return null
}

def shouldResumeOnFailure(Object... args) {
    def (failure, logger) = args

    if (failure instanceof AccountNotFoundException) {
        return true
    }
    return false
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
