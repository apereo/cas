# Account (Self-Service) Registration

Source: https://apereo.github.io/cas/development/registration/Account-Registration-Overview.html

CAS provides a modest workflow to handle self-service account registration. Once enabled, the account registration workflow allows users to register accounts with CAS to:

- Provide an initial, customizable set of details such as first name, last name, email to kickstart the account creation request.
- Receive an activation link with instructions via email or text message to verify the account creation request.
- Finalize the account creation request, choose a password, security questions, etc.
- Submit the account registration request to an identity manager system for provisioning and follow-up processes.
- Establish a single sign-on session for the user and redirect them to the original application, if any.

> **Usage Note**
>
> CAS is **NOT**, as of this writing, an identity management solution and does not intend to provide features or support capabilities that are typically found in such systems, such as complex provisioning workflows, account lifecycle management, inbound/outbound attribute mappings, etc. While all open-source software can be customized to no end, the capabilities described here ultimately expect one or more systems of record to hold and manage user accounts.

Account registration and sign-up functionality is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-account-mgmt"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.account-registration.core.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.account-registration.core.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.account-registration.core.password-policy-pattern` (Required; type `String`; default `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@$!%*?&])[A-Za-z\d$@$!%*?&]{8,10}`): A String value representing password policy regex pattern. Minimum 8 and Maximum 10 characters at least 1 Uppercase Alphabet, 1 Lowercase Alphabet, 1 Number and 1 Special Character.
- `cas.account-registration.core.registration-properties.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.account-registration.core.crypto.alg` (Optional; type `String`): The signing/encryption algorithm to use.
- `cas.account-registration.core.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.account-registration.core.crypto.encryption.key-size` (Optional; type `Integer`; default `512`): The encryption key size.
- `cas.account-registration.core.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.account-registration.core.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.
- `cas.account-registration.core.expiration` (Optional; type `String`; default `PT1M`): How long in minutes should the registration link remain valid.
- `cas.account-registration.core.include-client-ip-address` (Optional; type `Boolean`; default `true`): Whether the registration token will contain the client IP Address.
- `cas.account-registration.core.include-server-ip-address` (Optional; type `Boolean`; default `true`): Whether the registration token will contain the server IP Address.
- `cas.account-registration.core.security-questions-count` (Optional; type `Integer`; default `2`): Whether account registration should present security questions and how many, to complete the registration process.

**Signing & encryption**

This CAS feature is able to accept signing and encryption crypto keys. In most scenarios if keys are not provided, CAS will auto-generate them. The following instructions apply if you wish to manually and beforehand create the signing and encryption keys.

Note that if you are asked to create a [JWK](https://tools.ietf.org/html/rfc7517) of a certain size for the key, you are to use the following set of commands to generate the token:

```bash
wget https://raw.githubusercontent.com/apereo/cas/master/etc/jwk-gen.jar
java -jar jwk-gen.jar -t oct -s [size]
```

The outcome would be similar to:

```json
{
  "kty": "oct",
  "kid": "...",
  "k": "..."
}
```

The generated value for `k` needs to be assigned to the relevant CAS settings. Note that keys generated via the above algorithm are processed by CAS using the Advanced Encryption Standard (`AES`) algorithm which is a specification for the encryption of electronic data established by the U.S. National Institute of Standards and Technology.

---

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

## Account Registration Requests

[See this guide](https://apereo.github.io/cas/development/registration/Account-Registration-Requests.html) to learn more about the account registration request process and how to customize it.

### Account Registration Validation

The validation of registration requests can be customized using the following component:

```java
@Bean
public AccountRegistrationRequestValidator accountMgmtRegistrationRequestValidator() {
    return new MyAccountRegistrationRequestValidator();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.

## Communication Strategy

[See this guide](https://apereo.github.io/cas/development/registration/Account-Registration-Communication.html) to learn more.

## Username Extraction

By default, registration requests allow the user to choose a username, Construction and extraction of a `usename` field from the registration request can be customized using the following component:

```java
@Bean
public AccountRegistrationUsernameBuilder accountRegistrationUsernameBuilder() {
    return new MyAccountRegistrationUsernameBuilder();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
