# Groovy Principal Attribute Predicate - Multifactor Authentication Triggers

Source: https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication-Triggers-Global-PrincipalAttribute-Predicate.html

This is a more generic variant of the above trigger. It may be useful in cases where there is more than one provider configured and available in the application runtime and you need to design a strategy to dynamically decide on the provider that should be activated for the request.

The decision is handed off to a `Predicate` implementation defined in a Groovy script whose location is taught to CAS. The responsibility of the `test` function in the script is to determine eligibility of the provider to be triggered. If the predicate determines multiple providers as eligible by returning `true` more than one, the first provider in the sorted result set ranked by the provider’s order will be chosen to respond.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

The Groovy script predicate may be designed as such:

```groovy
import org.apereo.cas.authentication.*
import java.util.function.*
import org.apereo.cas.services.*

class PredicateExample implements Predicate<MultifactorAuthenticationProvider> {

    def service
    def principal
    def providers
    def logger

    public PredicateExample(service, principal, providers, logger) {
        this.service = service
        this.principal = principal
        this.providers = providers
        this.logger = logger
    }

    @Override
    boolean test(final MultifactorAuthenticationProvider p) {
        ...
    }
}
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `service` | The object representing the corresponding service definition in the registry. |
| `principal` | The object representing the authenticated principal. |
| `providers` | Collection of `MultifactorAuthenticationProvider`s from which a selection shall be made. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.triggers.principal.global-principal-attribute-predicate.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.mfa.triggers.principal.deny-if-unmatched` (Optional; type `Boolean`; default `false`): Force CAS to deny and block the authentication attempt altogether if attribute name/value configuration cannot produce a successful match to trigger multifactor authentication.
- `cas.authn.mfa.triggers.principal.global-principal-attribute-name-triggers` (Optional; type `String`): MFA can be triggered for all users/subjects carrying a specific attribute that matches one of the conditions below. - Trigger MFA based on a principal attribute(s) whose value(s) matches a regex pattern. Note that this behavior is only applicable if there is only a single MFA provider configured, since that would allow CAS to know what provider to next activate. - Trigger MFA based on a principal attribute(s) whose value(s) EXACTLY matches an MFA provider. This option is more relevant if you have more than one provider configured or if you have the flexibility of assigning provider ids to attributes as values. Needless to say, the attributes need to have been resolved for the principal prior to this step. Matching and comparison operations are case insensitive.
- `cas.authn.mfa.triggers.principal.global-principal-attribute-value-regex` (Optional; type `String`): The regular expression that is cross matched against the principal attribute to determine if the account is qualified for multifactor authentication. Matching and comparison operations are case insensitive.
- `cas.authn.mfa.triggers.principal.reverse-match` (Optional; type `Boolean`; default `false`): Principal attribute triggers by default look for a positive match and the presence of a pattern in attribute values. If you are looking to reverse that behavior and trigger MFA when the attribute value does NOT match the given pattern, then set this flag to `true`. This option does not apply when a predicate trigger is used to decide on the provider, and is only relevant when `#globalPrincipalAttributeNameTriggers` and `#globalPrincipalAttributeValueRegex` are used.

As an example, the following predicate example will begin to test each multifactor authentication provider and if the given provider is `mfa-duo` it will accept it as a valid trigger so long as the provider can be reached.

```groovy
import org.apereo.cas.authentication.*
import java.util.function.*
import org.apereo.cas.services.*

class PredicateExample implements Predicate<MultifactorAuthenticationProvider> {

    def service
    def principal
    def providers
    def logger

    public PredicateExample(service, principal, providers, logger) {
        this.service = service
        this.principal = principal
        this.providers = providers
        this.logger = logger
    }

    @Override
    boolean test(final MultifactorAuthenticationProvider p) {
        logger.info("Testing provider {}", p.getId())
        if (p.matches("mfa-duo")) {
           logger.info("Provider {} is available. Checking eligibility...", p.getId())
           if (p.isAvailable(this.service)) {
               logger.info("Provider {} matched. Good to go!", p.getId())
               return true;
           }
           logger.info("Skipping provider {}. Match failed.", p.getId())
           return false; 
        }
        logger.info("Provider {} cannot be reached", p.getId())
        return false
    }
}
```
