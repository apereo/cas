# Okta Attribute Resolution

Source: https://apereo.github.io/cas/development/integration/Attribute-Resolution-Okta.html

The following configuration describes how to fetch and retrieve attributes from [Okta instances](https://www.okta.com/).

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-okta-authentication"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.attribute-repository.okta.client-id` (Required; type `String`): Okta client id used in combination with the private key.
- `cas.authn.attribute-repository.okta.organization-url` (Required; type `String`): Okta domain.
- `cas.authn.attribute-repository.okta.private-key.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.attribute-repository.okta.scopes` (Required; type `List<String>`): Okta allows you to interact with Okta APIs using scoped OAuth 2.0 access tokens. Each access token enables the bearer to perform specific actions on specific Okta endpoints, with that ability controlled by which scopes the access token contains. Scopes are only used when using client id and private-key.
- `cas.authn.attribute-repository.okta.username-attribute` (Required; type `String`; default `username`): Username attribute to fetch attributes by.
- `cas.authn.attribute-repository.okta.api-token` (Optional; type `String`): Okta API token.
- `cas.authn.attribute-repository.okta.connection-timeout` (Optional; type `Integer`; default `5000`): Connection timeout in milliseconds.
- `cas.authn.attribute-repository.okta.id` (Optional; type `String`): A value can be assigned to this field to uniquely identify this resolver.
- `cas.authn.attribute-repository.okta.order` (Optional; type `Integer`): The order of this authentication handler in the chain.
- `cas.authn.attribute-repository.okta.proxy-host` (Optional; type `String`): Send requests via a proxy; define the hostname.
- `cas.authn.attribute-repository.okta.proxy-password` (Optional; type `String`): Send requests via a proxy; define the proxy password.
- `cas.authn.attribute-repository.okta.proxy-port` (Optional; type `Integer`; default `0`): Send requests via a proxy; define the proxy port. Negative/zero values should deactivate the proxy configuration for the http client.
- `cas.authn.attribute-repository.okta.proxy-username` (Optional; type `String`): Send requests via a proxy; define the proxy username.
