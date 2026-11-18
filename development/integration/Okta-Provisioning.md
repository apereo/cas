# Okta - Principal Provisioning

Source: https://apereo.github.io/cas/development/integration/Okta-Provisioning.html

CAS may be allowed to provision the authenticated principal to Okta. Successful login attempts would allow CAS to pass back the authenticated user profile to Okta, mapping the authenticated user attributes to the Okta user profile and either create or update the Okta account.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

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

- `cas.authn.okta.provisioning.client-id` (Required; type `String`): Okta client id used in combination with the private key.
- `cas.authn.okta.provisioning.organization-url` (Required; type `String`): Okta domain.
- `cas.authn.okta.provisioning.private-key.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.okta.provisioning.scopes` (Required; type `List<String>`): Okta allows you to interact with Okta APIs using scoped OAuth 2.0 access tokens. Each access token enables the bearer to perform specific actions on specific Okta endpoints, with that ability controlled by which scopes the access token contains. Scopes are only used when using client id and private-key.
- `cas.authn.okta.provisioning.api-token` (Optional; type `String`): Okta API token.
- `cas.authn.okta.provisioning.attribute-mappings` (Optional; type `Map<String,String>`): Map of attributes that optionally may be used to control the names of the attributes to Okta that form the user profile. If an attribute is provided by Okta, such as `department`, it can be listed here as the key of the map with a value that should be the name of that attribute as collected and recorded by CAS. For example, the convention `department->organization` will process the CAS attribute `organization` and will assign its value to the user profile under `department`. If no mapping is specified, the okta attribute itself will be used to find the CAS principal attribute value.
- `cas.authn.okta.provisioning.connection-timeout` (Optional; type `Integer`; default `5000`): Connection timeout in milliseconds.
- `cas.authn.okta.provisioning.enabled` (Optional; type `Boolean`; default `false`): Whether or not provisioning should be enabled with Okta.
- `cas.authn.okta.provisioning.order` (Optional; type `Integer`): The order of this authentication handler in the chain.
- `cas.authn.okta.provisioning.proxy-host` (Optional; type `String`): Send requests via a proxy; define the hostname.
- `cas.authn.okta.provisioning.proxy-password` (Optional; type `String`): Send requests via a proxy; define the proxy password.
- `cas.authn.okta.provisioning.proxy-port` (Optional; type `Integer`; default `0`): Send requests via a proxy; define the proxy port. Negative/zero values should deactivate the proxy configuration for the http client.
- `cas.authn.okta.provisioning.proxy-username` (Optional; type `String`): Send requests via a proxy; define the proxy username.
