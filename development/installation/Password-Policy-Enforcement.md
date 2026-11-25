# Password Policy Enforcement

Source: https://apereo.github.io/cas/development/installation/Password-Policy-Enforcement.html

Password policy enforcement attempts to:

- Detect a number of scenarios that would otherwise prevent user authentication based on user account status.
- Warn users whose account status is near a configurable expiration date and redirect the flow to an external identity management system.

## LDAP

The below scenarios are by default considered errors preventing authentication in a generic manner through the normal CAS login flow. LPPE intercepts the authentication flow, detecting the above standard error codes. Error codes are then translated into proper messages in the CAS login flow and would allow the user to take proper action, fully explaining the nature of the problem.

- `ACCOUNT_LOCKED`
- `ACCOUNT_DISABLED`
- `ACCOUNT_EXPIRED`
- `INVALID_LOGON_HOURS`
- `INVALID_WORKSTATION`
- `PASSWORD_MUST_CHANGE`
- `PASSWORD_EXPIRED`

The translation of LDAP errors into CAS workflow is all handled by [ldaptive](http://www.ldaptive.org/docs/guide/authentication/accountstate).

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.ldap[0].password-policy.groovy.location` (Required; type `Resource`): Handle password policy via Groovy script. The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.ldap[0].password-policy.account-state-handling-enabled` (Optional; type `boolean`; default `true`): Indicates whether account state handling should be enabled to process warnings or errors reported back from the authentication response, produced by the source.
- `cas.authn.ldap[0].password-policy.custom-policy-class` (Optional; type `String`): An implementation of a policy class that knows how to handle LDAP responses. The class must be an implementation of `org.ldaptive.auth.AuthenticationResponseHandler`.
- `cas.authn.ldap[0].password-policy.display-warning-on-match` (Optional; type `boolean`; default `true`): Indicates if warning should be displayed, when the ldap attribute value matches the `#warningAttributeValue`.
- `cas.authn.ldap[0].password-policy.enabled` (Optional; type `boolean`; default `true`): Whether password policy should be enabled.
- `cas.authn.ldap[0].password-policy.login-failures` (Optional; type `int`; default `5`): When dealing with FreeIPA, indicates the number of allows login failures.
- `cas.authn.ldap[0].password-policy.password-expiration-number-of-days` (Optional; type `int`; default `180`): This is used to calculate an expiration period for the account password. When defined, LDAP password policy handling will use the `pwdLastSet` attribute which must be returned from the LDAP authentication attempt. LDAP password policy handling will emit a warning for the `pwdLastSet` value plus the expiration amount. A negative value will disable the operations that calculate the expiration period.
- `cas.authn.ldap[0].password-policy.policy-attributes` (Optional; type `Map<String, Class<? extends LoginException>>`): Key-value structure (Map) that indicates a list of boolean attributes as keys. If either attribute value is true, indicating an account state is flagged, the corresponding error can be thrown. Example `accountLocked=javax.security.auth.login.AccountLockedException`
- `cas.authn.ldap[0].password-policy.strategy` (Optional; type `PasswordPolicyProperties.PasswordPolicyHandlingOptions`; default `DEFAULT`): Decide how authentication should handle password policy changes. Available values are as follows: - `DEFAULT`: Default option to handle policy changes. - `GROOVY`: Handle account password policies via Groovy. - `REJECT_RESULT_CODE`: Strategy to only activate password policy if the authentication response code is not blocked.
- `cas.authn.ldap[0].password-policy.type` (Optional; type `AbstractLdapProperties.LdapType`; default `GENERIC`): LDAP type.
- `cas.authn.ldap[0].password-policy.warn-all` (Optional; type `boolean`): Always display the password expiration warning regardless.
- `cas.authn.ldap[0].password-policy.warning-attribute-name` (Optional; type `String`): Used by an account state handling policy that only calculates account warnings in case the entry carries this attribute.
- `cas.authn.ldap[0].password-policy.warning-attribute-value` (Optional; type `String`): Used by an account state handling policy that only calculates account warnings in case the entry carries an attribute `#warningAttributeName` whose value matches this field.
- `cas.authn.ldap[0].password-policy.warning-days` (Optional; type `int`; default `30`): This is used to calculate a warning period to see if account expiry is within the calculated window.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

**LDAP & Active Directory**

**LDAP Scriptable Search Filter**

LDAP search filters can point to an external Groovy script to dynamically construct the final filter template.

The script itself may be designed as:

```groovy
import org.ldaptive.*
import org.springframework.context.*

def run(Object[] args) {
    def (filter,parameters,applicationContext,logger) = args

    logger.info("Configuring LDAP filter")
    filter.setFilter("uid=something")
}
```

The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `filter` | `FilterTemplate` to be updated by the script and used for the LDAP query. |
| `parameters` | Map of query parameters which may be used to construct the final filter. |
| `applicationContext` | Reference to the Spring `ApplicationContext` reference. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

**Password policy**

**Password Policy Strategies**

If the password policy strategy is to be handed off to a Groovy script, the outline of the script may be as follows:

```groovy
import java.util.*
import org.ldaptive.auth.*
import org.apereo.cas.*
import org.apereo.cas.authentication.*
import org.apereo.cas.authentication.support.*

List<MessageDescriptor> run(final Object... args) {
    def (response,configuration,logger,applicationContext) = args
    logger.info("Handling password policy [{}] via ${configuration.getAccountStateHandler()}", response)

    def accountStateHandler = configuration.getAccountStateHandler()
    return accountStateHandler.handle(response, configuration)
}
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `response` | The LDAP authentication response of type `org.ldaptive.auth.AuthenticationResponse` |
| `configuration` | The LDAP password policy configuration carrying the account state handler defined. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |
| `applicationContext` | The Spring `ApplicationContext` that allows one to interact with the runtime. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

### Account Expiration Notification

LPPE is also able to warn the user when the account is about to expire. The expiration policy is determined through pre-configured LDAP attributes with default values in place.

## JDBC

A certain number of database authentication schemes have limited support for detecting locked/disabled/etc accounts via column names that are defined in the CAS settings. You will need to consult the documentation for the relevant database authentication strategy and type and configure CAS to enforce password policy checks.
