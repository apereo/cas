---
layout: default
title: CAS - Multifactor Authentication Bypass
category: Multifactor Authentication
---

{% include variables.html %}

# Multifactor Authentication Bypass

Each [multifactor provider](Configuring-Multifactor-Authentication.html) is equipped with 
options to allow for bypass. Once the provider
is chosen to honor the authentication request, bypass rules are then consulted
to calculate whether the provider should ignore the request and skip MFA conditionally.

## Default Bypass

CAS provides a default bypass policy for 
each [multifactor provider](Configuring-Multifactor-Authentication.html) that can be configured through CAS properties.  
All providers will consult this policy for bypass events before consulting any other configured bypass providers.

Bypass rules allow for the following options for each provider:

- Skip multifactor authentication based on designated **principal** attribute **names**.
- ...[and optionally] Skip multifactor authentication based on designated **principal** attribute **values**.
- Skip multifactor authentication based on designated **authentication** attribute **names**.
- ...[and optionally] Skip multifactor authentication based on designated **authentication** attribute **values**.
- Skip multifactor authentication depending on method/form of primary authentication execution.
- Skip multifactor authentication depending on the properties of the http request such as remote addr/host and/or header names.

A few examples follow:

- Trigger MFA except when the principal carries an `affiliation` attribute whose value is either `alum` or `member`.
- Trigger MFA except when the principal carries a `superAdmin` attribute.
- Trigger MFA except if the method of primary authentication is SPNEGO.
- Trigger MFA except if credentials used for primary authentication are of type `org.example.MyCredential`.

Note that in addition to the above options, some multifactor authentication providers
may also skip and bypass the authentication request in the event that the authenticated principal does not quite "qualify"
for multifactor authentication. See the documentation for each specific provider to learn more.

### Configuration

Multifactor authentication bypass configuration is defined for each provider id. To learn more about the available settings,
examine the configuration for your choice of multifactor authentication provider.

Note that ticket validation requests shall successfully go through if multifactor authentication is
bypassed for the given provider. In such cases, no authentication context is passed back to the application and
additional attributes are supplanted to let the application know multifactor authentication is bypassed for the provider.

### Bypass Per Service

MFA Bypass rules can be overridden per application via the CAS service registry. This is useful when
MFA may be turned on globally for all applications and services, yet a few selectively need to be excluded. Services
whose access should bypass MFA may be defined as such in the CAS service registry:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "multifactorAuthenticationProviders" : [ "java.util.LinkedHashSet", [ "mfa-duo" ] ],
    "bypassEnabled" : "true"
  }
}
```

### Bypass Per Principal Attribute & Service

This is similar to the above option, except that bypass is only activated for
the registered application if the authenticated principal contains an attribute
with the specified value(s). 

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "bypassPrincipalAttributeName": "attributeForBypass",
    "bypassPrincipalAttributeValue": "^bypass-value-[A-Z].+"
    "bypassIfMissingPrincipalAttribute": false
  }
}
```

Matching and comparison operations are case insensitive.

<div class="alert alert-info">:information_source: <strong>Remember</strong>
<p>Setting the <code>bypassEnabled</code> flag here is unnecessary and may cause side-effects. Once principal attribute name and match value
are defined, the <code>bypassEnabled</code> is expected to be <code>true</code> anyway.</p>
</div>

## Other Bypass Providers

In addition to the configurable default bypass rules, the following bypass providers 
can be defined and executed after default bypass rules are calculated.

Remember that the following bypass policies are defined per multifactor authentication provider.
You will need to instruct CAS to activate a bypass policy based on the options listed below for the multifactor authentication in question.
Each provider should have its own dedicated settings and properties that would allow you control its own bypass rules.
  
{% tabs bypassproviders %}

{% tab bypassproviders <i class="fa fa-file-code px-1"></i>Groovy %}

Multifactor authentication bypass may be determined using a Groovy script of your
own design. The outcome of the script, if `true` indicates that multifactor
authentication for the requested provider should proceed. Otherwise `false` indicates
that multifactor authentication for this provider should be skipped and bypassed.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

The outline of the script may be as follows:

```groovy
import java.util.*

boolean run(final Object... args) {
    def (authentication,principal,registeredService,provider,logger,httpRequest) = args
    
    // Stuff happens...

    return false;
}
```

The parameters passed are as follows:

| Parameter        | Description                                                                   |
|------------------|-------------------------------------------------------------------------------|
| `authentication` | The object representing the established authentication event.                 |
| `principal`      | The object representing the authenticated principal.                          |
| `service`        | The object representing the corresponding service definition in the registry. |
| `provider`       | The object representing the requested multifactor authentication provider.    |
| `logger`         | The object responsible for issuing log messages such as `logger.info(...)`.   |
| `httpRequest`    | The object responsible for capturing the http request.                        |

As an example, the following script skips multifactor authentication if the application
requesting it is registered in the CAS service registry under the name `MyApplication` and
only does so if the provider is Duo Security and the authenticated principal contains
an attribute named `mustBypassMfa` whose values contains `true`.

```groovy
boolean run(final Object... args) {
    def (authentication,principal,service,provider,logger,httpRequest) = args
    if (service.name == "MyApplication") {
        logger.info("Evaluating principal attributes ${principal.attributes}")

        def bypass = principal.attributes['mustBypassMfa']
        if (bypass.contains("true") && provider.id == "mfa-duo") {
            logger.info("Skipping bypass for principal ${principal.id}")
            return false
        }
    }
    return true
}
```

{% endtab %}

{% tab bypassproviders REST %}

Multifactor authentication bypass may be determined using a REST API of your
own design. Endpoints must be designed to accept/process `application/json` via
`GET` requests. A returned status code for `2xx` indicates that multifactor
authentication for the requested provider should proceed. Otherwise multifactor
authentication for this provider should be skipped and bypassed.

The following parameters are passed:

| Parameter   | Description                                                       |
|-------------|-------------------------------------------------------------------|
| `principal` | The identifier of the authenticated principal.                    |
| `provider`  | The identifier of the multifactor authentication provider.        |
| `service`   | The identifier of the registered service in the registry, if any. |


{% endtab %}

{% endtabs %}
