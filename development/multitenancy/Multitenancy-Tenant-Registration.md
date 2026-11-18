# Tenant Registration - Multitenancy

Source: https://apereo.github.io/cas/development/multitenancy/Multitenancy-Tenant-Registration.html

Each tenant must be registered with CAS before it can be used. Tenant definitions are read from a JSON file by default, and you can replace that with your own tenant registry.

Tenants are registered with CAS via a JSON file by default that is expected to be available at a well-known location and automatically watched for changes.

The following settings and properties are available from the CAS configuration catalog:

- `cas.multitenancy.json.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

The basic construct for a tenant definition should match the following:

```json
[
  "java.util.ArrayList",
  [
    {
      "@class": "org.apereo.cas.multitenancy.TenantDefinition",
      "id": "shire",
      "description": "This is my tenant description",
      "properties": {
        "@class": "java.util.LinkedHashMap",
        "key": "value"
      },
      "authenticationPolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantAuthenticationPolicy",
        "authenticationHandlers": [ "java.util.ArrayList", [ "LdapAuthHandler1" ] ],
        "attributeRepositories": [ "java.util.ArrayList", [ "AttributeRepository1" ] ],
        "authenticationProtocolPolicy": {
          "@class": "org.apereo.cas.multitenancy.TenantCasAuthenticationProtocolPolicy",
          "supportedProtocols": [ "java.util.HashSet", [ "SAML1", "CAS20", "CAS30" ] ]
        }
      },
      "delegatedAuthenticationPolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantDelegatedAuthenticationPolicy",
        "allowedProviders": [ "java.util.ArrayList", [ "..." ] ]
      },
      "userInterfacePolicy": {
        "@class": "org.apereo.cas.multitenancy.DefaultTenantUserInterfacePolicy",
        "themeName": "shire"
      }
    }
  ]
]
```

## Custom Tenant Registration

If you need to customize the tenant registration process, you may do so by providing a custom implementation of the following bean definition:

```java
@Bean
public TenantsManager tenantsManager() {
    return new MyTenantsManager();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
