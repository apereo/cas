### Password Policy Configuration

The following  options are shared and apply when CAS is configured to integrate with 
account sources and authentication strategies that support password policy 
enforcement and detection. Note that certain setting may only be applicable if the 
underlying account source is LDAP and are only taken into account if the 
authentication strategy configured in CAS is able to honor and recognize them:

```properties
# ${configurationKey}.type=GENERIC|AD|FreeIPA|EDirectory

# ${configurationKey}.enabled=true
# ${configurationKey}.policy-attributes.account-locked=javax.security.auth.login.AccountLockedException
# ${configurationKey}.login-failures=5
# ${configurationKey}.warning-attribute-value=
# ${configurationKey}.warning-attribute-name=
# ${configurationKey}.display-warning-on-match=true
# ${configurationKey}.warn-all=true
# ${configurationKey}.warning-days=30
# ${configurationKey}.account-state-handling-enabled=true

# An implementation of `org.ldaptive.auth.AuthenticationResponseHandler`
# ${configurationKey}.custom-policy-class=com.example.MyAuthenticationResponseHandler

# ${configurationKey}.strategy=DEFAULT|GROOVY|REJECT_RESULT_CODE
# ${configurationKey}.groovy.location=file:/etc/cas/config/password-policy.groovy
```

### Password Policy Strategies

Password policy strategy types are outlined below. The strategy evaluates the authentication 
response received from LDAP, etc and is allowed to review it upfront in order to further 
examine whether account state, messages and warnings is eligible for further investigation.

| Option        | Description
|---------------|-----------------------------------------------------------------------------
| `DEFAULT`     | Accepts the authentication response as is, and processes account state, if any.
| `GROOVY`      | Examine the authentication response as part of a Groovy script dynamically. The responsibility of handling account state changes and warnings is entirely delegated to the script.
| `REJECT_RESULT_CODE`  | An extension of the `DEFAULT` where account state is processed only if the result code of the authentication response is not denied in the configuration. By default `INVALID_CREDENTIALS(49)` prevents CAS from handling account states.

If the password policy strategy is to be handed off to a Groovy script, the outline of the script may be as follows:

```groovy
import java.util.*
import org.ldaptive.auth.*
import org.apereo.cas.*
import org.apereo.cas.authentication.*
import org.apereo.cas.authentication.support.*

def List<MessageDescriptor> run(final Object... args) {
    def response = args[0]
    def configuration = args[1];
    def logger = args[2]
    def applicationContext = args[3]

    logger.info("Handling password policy [{}] via ${configuration.getAccountStateHandler()}", response)

    def accountStateHandler = configuration.getAccountStateHandler()
    return accountStateHandler.handle(response, configuration)
}
```

The parameters passed are as follows:

| Parameter             | Description
|-----------------------|-----------------------------------------------------------------------------------
| `response`            | The LDAP authentication response of type `org.ldaptive.auth.AuthenticationResponse`
| `configuration`       | The LDAP password policy configuration carrying the account state handler defined.
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`.
