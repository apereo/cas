# YAML Service Registry

Source: https://apereo.github.io/cas/development/services/YAML-Service-Management.html

This registry reads services definitions from YAML configuration files at the application context initialization time. YAML files are expected to be found inside a configured directory location and this registry will recursively look through the directory structure to find relevant files.

Support is enabled by adding the following module into the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-yaml-service-registry"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.service-registry.yaml.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.service-registry.yaml.order` (Optional; type `Integer`)
- `cas.service-registry.yaml.watcher-enabled` (Optional; type `Boolean`; default `true`): Flag indicating whether a background watcher thread is enabled for the purposes of live reloading of service registry data changes from persistent data store.

A sample YAML file follows:

```yml
--- !<org.apereo.cas.services.CasRegisteredService>
serviceId: "testId"
name: "YAML"
id: 1000
description: "description"
attributeReleasePolicy: !<org.apereo.cas.services.ReturnAllAttributeReleasePolicy> {}
accessStrategy: !<org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy>
  enabled: true
  ssoEnabled: true
```

> **YAML Validation**
>
> The tags containing classname hints (`!<classname>`) cause problems with many YAML validators. If you need to validate your YAML, try removing those tags for validation. Remember that an empty map (`{}`) may be required after the tag if you are not including any attributes for a property.

> **Clustering Services**
>
> You MUST consider that if your CAS server deployment is clustered, each CAS node in the cluster must have access to the same set of configuration files as the other, or you may have to devise a strategy to keep changes synchronized from one node to the next.

The service registry is also able to auto detect changes to the specified directory. It will monitor changes to recognize file additions, removals and updates and will auto-refresh CAS so changes do happen instantly.

> **Escaping Characters**
>
> Please make sure all field values in the blob are correctly escaped, especially for the service id. If the service is defined as a regular expression, certain regex constructs such as "." and "\d" need to be doubly escaped.

## Naming Conventions

The naming convention for new files is recommended to be the following:

```bash
YAML fileName = serviceName + "-" + serviceNumericId + ".yml"
```

Remember that because files are created based on the `serviceName`, you will need to make sure [characters considered invalid for file names](https://en.wikipedia.org/wiki/Filename#Reserved_characters_and_words) are not used as part of the name. Furthermore, note that CAS **MUST** be given full read/write permissions on directory which contains service definition files.

The registry is able to auto-organize service definition files into dedicated directories based on the service type. If any of the following subdirectories exist inside the base services directory for a CAS service, CAS would auto-choose the appropriate directory by type:

| Service Type | Subdirectories |
| --- | --- |
| CAS | `CAS Client`, `cas-client` |
| OpenID Connect | `OpenID Connect Relying Party`, `openid-connect-relying-party` |
| OAuth2 | `OAuth2 Client`, `oauth2-client` |
| SAML2 | `SAML2 Service Provider`, `saml2-service-provider` |
| WS-Federation | `WS Federation Relying Party`, `ws-federation-relying-party` |

If no subdirectory for a service type is found, the base services directory would be used.

> **Duplicate Services**
>
> As you add more files to the directory, you need to be absolutely sure that no two service definitions would have the same numeric id. If this happens, loading one definition will stop loading the other. While service ids can be chosen arbitrarily, make sure all service numeric identifiers are unique. CAS will also output warnings if duplicate data is found.

## Replication

If CAS is deployed in a cluster, the service definition files must be kept in sync for all CAS nodes. Please [review this guide](https://apereo.github.io/cas/development/services/Configuring-Service-Replication.html) to learn more about available options.
