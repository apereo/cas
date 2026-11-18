# Password Management

Source: https://apereo.github.io/cas/development/password_management/Password-Management.html

If authentication fails due to a rejected password policy, CAS is able to intercept that request and allow the user to update the account password in place. The password management features of CAS are rather modest, and alternatively should the functionality provide inadequate for your policy, you may always redirect CAS to use a separate and standalone application that is fully in charge of managing the account password and associated flows.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
        Including this module in the CAS WAR overlay is optional and unnecessary. This module is automatically included
        and bundled with the CAS server distribution and there are alternative options baked in to allow one to replace
        this module with another. The entry below is listed for reference only.
    */
    implementation "org.apereo.cas:cas-server-support-pm-webflow"
}
```

> **YAGNI**
>
> You do not need to explicitly include this module in your configuration and overlays. This is just to tell you that it exists.

## Configuration

To learn more about available notification options, please [see this guide](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration.html) or [this guide](https://apereo.github.io/cas/development/notifications/Sending-Email-Configuration.html).

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pm.core.enabled` (Required; type `Boolean`; default `false`): Flag to indicate if password management facility is enabled.
- `cas.authn.pm.core.password-policy-pattern` (Required; type `String`; default `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@$!%*?&])[A-Za-z\d$@$!%*?&]{8,10}`): A String value representing password policy regex pattern. Minimum 8 and Maximum 10 characters at least 1 Uppercase Alphabet, 1 Lowercase Alphabet, 1 Number and 1 Special Character.
- `cas.authn.pm.core.auto-login` (Optional; type `Boolean`; default `false`): Flag to indicate whether successful password change should trigger login automatically.
- `cas.authn.pm.core.password-policy-character-set` (Optional; type `String`; default `abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789![]{}()%&*$#^<>~@|`): The character set that CAS may use to generate and suggest new passwords.
- `cas.authn.pm.core.password-policy-password-length` (Optional; type `Long`; default `10`): The password length used by CAS when suggesting generated passwords.
- `cas.authn.pm.history.core.enabled` (Optional; type `Boolean`; default `false`): Flag to indicate if password history tracking is enabled.
- `cas.authn.pm.webflow.enabled` (Optional; type `Boolean`; default `true`): Whether webflow auto-configuration should be enabled.
- `cas.authn.pm.webflow.order` (Optional; type `Integer`; default `0`): The order in which the webflow is configured.

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

## Password Reset

Please [see this guide](https://apereo.github.io/cas/development/password_management/Password-Management-Reset.html) for more details.

## Account Management

CAS may also allow individual end-users to update certain aspects of their account that relate to password management in a *mini portal* like setup, such as resetting the password or updating security questions, etc. Please [see this guide](https://apereo.github.io/cas/development/registration/Account-Management-Overview.html) for more details.

## Forgot Username

To learn more, please [see this guide](https://apereo.github.io/cas/development/password_management/Password-Management-ForgotUsername.html).

## Password History

To learn more, please [see this guide](https://apereo.github.io/cas/development/password_management/Password-Management-History.html).

## Storage

User accounts can be found via the following ways.

| Storage | Instructions |
| --- | --- |
| JSON | [See this guide](https://apereo.github.io/cas/development/password_management/Password-Management-JSON.html). |
| Groovy | [See this guide](https://apereo.github.io/cas/development/password_management/Password-Management-Groovy.html). |
| LDAP | [See this guide](https://apereo.github.io/cas/development/password_management/Password-Management-LDAP.html). |
| JDBC | [See this guide](https://apereo.github.io/cas/development/password_management/Password-Management-JDBC.html). |
| REST | [See this guide](https://apereo.github.io/cas/development/password_management/Password-Management-REST.html). |
| Custom | [See this guide](https://apereo.github.io/cas/development/password_management/Password-Management-Custom.html). |
