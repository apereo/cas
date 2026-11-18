# JSON Service Registry

Source: https://apereo.github.io/cas/development/services/JSON-Service-Management.html

This registry reads services definitions from JSON configuration files at the application context initialization time. JSON files are expected to be found inside a configured directory location and this registry will recursively look through the directory structure to find relevant JSON files.

Support is enabled by adding the following module into the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-json-service-registry"
}
```

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testJsonFile",
  "id" : 103935657744185,
  "evaluationOrder" : 10
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.service-registry.json.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.service-registry.json.order` (Optional; type `Integer`)
- `cas.service-registry.json.watcher-enabled` (Optional; type `Boolean`; default `true`): Flag indicating whether a background watcher thread is enabled for the purposes of live reloading of service registry data changes from persistent data store.

> **Clustering Services**
>
> You MUST consider that if your CAS server deployment is clustered, each CAS node in the cluster must have access to the same set of JSON configuration files as the other, or you may have to devise a strategy to keep changes synchronized from one node to the next.

The JSON service registry is also able to auto detect changes to the specified directory. It will monitor changes to recognize file additions, removals and updates and will auto-refresh CAS so changes do happen instantly.

> **Escaping Characters**
>
> Please make sure all field values in the JSON blob are correctly escaped, especially for the service id. If the service is defined as a regular expression, certain regex constructs such as `.` and `\d` need to be doubly escaped.

## Naming Conventions

The naming convention for new JSON files is recommended to be the following:

```bash
JSON fileName = serviceName + "-" + serviceNumericId + ".json"
```

Based on the above formula, for example the above JSON snippet shall be named: `testJsonFile-103935657744185.json`. Remember that because files are created based on the `serviceName`, you will need to make sure [characters considered invalid for file names](https://en.wikipedia.org/wiki/Filename#Reserved_characters_and_words) are not used as part of the name. Furthermore, note that CAS **MUST** be given full read/write permissions on directory which contains service definition files.

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

## JSON Syntax

CAS uses [a version of the JSON syntax](https://hjson.github.io/) that provides a much more relaxed syntax with the ability to specify comments.

**Comments**

A given JSON file for instance could be formatted as such in CAS:

```json
{
  /*
    Generic service definition that applies to https/imaps urls
    that wish to register with CAS for authentication.
  */
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "name" : "HTTPS and IMAPS",
  # "description": "This is commented out"
  "id" : 10000001,
}
```

Note the trailing comma at the end. See the above link for more info on the alternative syntax.

**Multiline Strings**

Multiline strings with proper whitespace handling should also be supported:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://app.example.org/.*",
  "id": 1,
  "name": "Sample",
  "description": 
    '''
    This is the description
    of this application here
    '''
}
```

## Clustering & Replication

If CAS is deployed in a cluster, the service definition files must be kept in sync for all CAS nodes. Please [review this guide](https://apereo.github.io/cas/development/services/Configuring-Service-Replication.html) to learn more about available options.

You might be wondering: *but, is using the file system with JSON ([or YAML](https://apereo.github.io/cas/development/services/YAML-Service-Management.html)) files a good solution for CAS deployments?*

Like almost everything else in life, it depends.

If you have a single CAS server, packaging the JSON files with the CAS web application is easy. You just have to remember (which might be the downside) to redeploy and rebuild CAS every time you add, remove or make changes to any of the files. Your build then becomes immutable which may or may not be a good thing. You may alternatively decide to not package these files with the CAS web application and instead move them outside to a different location, removing the need for rebuilding. You then have to worry about where that location might be, and what permissions it should be given to the process that reads data from it etc, and how are changes to files in that directory tracked and managed by source control.

If you have `N` CAS server nodes in a cluster, you should consider the following:

- If the JSON files are packaged with the CAS web application, you have to rebuild and redeploy CAS `N` times.
- If the JSON files are outside the CAS web application, you need to make sure `N` CAS servers in the cluster have access to the same shared location, or you may look at replication options [outlined here](https://apereo.github.io/cas/development/services/Configuring-Service-Replication.html). Managing files in an external location not tied to the CAS build *might* make it challenging for you to track such changes in source control.

You may alternatively decide to not use the filesystem and instead opt for a proper external *data store*. Then, you would of course remove issues regarding rebuilds and keeping files in sync across CAS nodes because the database must be centralized and shared by all nodes. Instead, you add the extra overhead of managing, updating and maintaining a separate database which generally should remain available as much as CAS itself.

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](https://apereo.github.io/cas/development/services/AutoInitialization-Service-Management.html) for more info.
