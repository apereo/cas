# Activation - Attribute Consent

Source: https://apereo.github.io/cas/development/integration/Attribute-Release-Consent-Activation.html

Attribute consent activation can be controlled both at a global and per-service level. By default, global activation rules for attribute consent are turned on and the consent policy rules for the service definition are disabled. The consent policy for the service definition may override the global rules using the `status` field which accepts the following values:

| Value | Description |
| --- | --- |
| `FALSE` | Consent policy is disabled, overriding the global configuration. |
| `TRUE` | Consent policy is enabled, overriding the global configuration. |
| `UNDEFINED` | Consent policy is undefined, delegating the decision to the global configuration. |

Note that attribute consent policies may also be chained together to compose multiple policies. Each policy can be individually disabled or enabled and the overall aggregate status of the entire attribute consent policy will be used to determine attribute consent activation and selection. A sample chain of attribute consent polices follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "description" : "sample",
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.services.ChainingAttributeReleasePolicy",
    "policies": [ "java.util.ArrayList",
      [
        {
          "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
          "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ],
          "consentPolicy": {
            "@class": "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
            "includeOnlyAttributes": ["java.util.LinkedHashSet", ["cn"]],
            "status": "TRUE"
          }
        },
        {
          "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
          "allowedAttributes" : [ "java.util.ArrayList", [ "displayName" ] ],
          "consentPolicy": {
            "@class": "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
            "includeOnlyAttributes": ["java.util.LinkedHashSet", ["displayName"]],
            "status": "FALSE"
          }
        }
      ]
    ]
  }
}
```

## Activation via Groovy

The default consent activation strategy can be replaced with an external Groovy script to determine whether the request qualifies for consent. Path to the script is defined via CAS configuration properties.

The following settings and properties are available from the CAS configuration catalog:

- `cas.consent.activation-strategy-groovy-script.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

The script itself may be designed as such:

```groovy
import org.apereo.cas.configuration.support.*
import org.apereo.cas.consent.*

def run(Object[] args) {
    def (consentEngine,casProperties,service,registeredService,authentication,request,logger) = args
    logger.debug("Activating consent for ${registeredService.name}")
    // calculate whether consent is required for the service and return the result
    def required = false
    return required ? ConsentQueryResult.required() : ConsentQueryResult.ignored()
}
```

The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `consentEngine` | A reference to the `ConsentEngine` object. |
| `casProperties` | A reference to the CAS configuration properties loaded from property sources. |
| `service` | The `Service` object representing the requesting application. |
| `registeredService` | The `RegisteredService` object representing the service definition in the registry. |
| `authentication` | The `Authentication` object representing the active authentication transaction. |
| `request` | The object representing the HTTP servlet request. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

The script is expected to return either `true` or `false` to determine whether or not consent is required.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
