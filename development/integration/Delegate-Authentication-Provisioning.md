# Delegated Authentication Provisioning

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-Provisioning.html

By default, user profiles that are extracted from external identity providers and merged into a CAS authenticated principal are not stored or tracked anywhere. CAS does provide additional options to allow such profiles to be managed outside of CAS and/or provisioned into identity stores, allowing you optionally to link external/guest accounts with their equivalent found in the authentication source used by CAS, etc.

**Groovy**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.provisioning.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

Provisioning tasks can be carried out using an external Groovy script with the following structure:

```groovy
def run(Object[] args) {
    def (principal,userProfile,client,logger) = args
    ...
}
```

It is not expected for the script to return a value. The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `principal` | CAS authenticated `Principal` that contains all attributes and claims. |
| `userProfile` | The original `UserProfile` extracted from the external identity provider. |
| `client` | The `Client` configuration responsible for the exchange between CAS and the identity provider. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

**REST**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.provisioning.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.pac4j.provisioning.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.pac4j.provisioning.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.pac4j.provisioning.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.pac4j.provisioning.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.pac4j.provisioning.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.

Provisioning tasks can be carried out using an external REST endpoint expected to receive the following in the request body:

| Attribute | Description |
| --- | --- |
| `principalId` | CAS authenticated principal identifier. |
| `principalAttributes` | CAS authenticated principal attributes. |
| `profileId` | The identifier of the user profile extracted from the identity provider. |
| `profileTypedId` | The *typed* identifier of the user profile extracted from the identity provider. |
| `profileAttributes` | Collection of attributes extracted from the identity provider’s response. |
| `clientName` | The client name responsible for the exchange between CAS and the identity provider. |

**SCIM**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.provisioning.scim.enabled` (Optional; type `Boolean`; default `false`): Whether provisioning to SCIM targets should be enabled for delegated authentication attempts.

Provisioning tasks can be carried out using the CAS [SCIM Provisioning](https://apereo.github.io/cas/development/integration/SCIM-Provisioning.html). Once enabled and configured, authenticated profiles established from the external identity provider may be provisioned via SCIM to other systems.

> **Usage**
>
> SCIM integration support for delegated authentication is only handled via SCIM `v2`.

**Custom**

If you wish to create your own provisioner for delegated authentication, you will need to design a component and register it with CAS as such:

```java
@Bean
public DelegatedClientUserProfileProvisioner clientUserProfileProvisioner() {
    return new CustomDelegatedClientUserProfileProvisioner();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
