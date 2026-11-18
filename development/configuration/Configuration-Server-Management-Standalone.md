# Configuration Server - Standalone Profile

Source: https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-Standalone.html

This is the default configuration mode which indicates that CAS does **NOT** require connections to an external configuration server and will run in an embedded *standalone mode*. When this option is turned on, CAS by default will attempt to locate settings and properties inside a pre-defined directories and files and otherwise falls back to typically using `/etc/cas/config` as the configuration directory.

Similar to the Spring Cloud external configuration server, the contents of this directory include `(cas|application).(yml|properties)` files that can be used to control CAS behavior. Also, note that this configuration directory can be monitored by CAS to auto-pick up changes and refresh the application context as needed. Please [review this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Reload.html#reload-strategy) to learn more.

Note that by default, all CAS settings and configuration is controlled via the embedded `application.properties` file in the CAS server web application. There is also an embedded `application.yml` file that allows you to override all defaults if you wish to ship the configuration inside the main CAS web application and not rely on externalized configuration files. If you prefer properties to yaml, then `application-standalone.properties` will override `application.properties` as well.

Settings found in external configuration files are and will be able to override the defaults provided by CAS. The naming of the configuration files inside the CAS configuration directory follows the below pattern:

- An `application.(properties|yml|yaml)` file is always loaded, if found.
- Settings located inside `properties|yml|yaml` files whose name matches the application name are loaded (i.e `cas.properties`). Note: the application name defaults to uppercase `CAS` but the lowercase name will also be loaded.
- Settings located inside `properties|yml|yaml` files whose name matches one of the active profiles are loaded (i.e `ldap.properties`).
- Profile-specific application properties outside of your packaged web application (`application-{profile}.properties|yml|yaml`) This allows you to, if needed, split your settings into multiple property files and then locate them by assigning their name to the list of active profiles (i.e. `standalone,testldap,stagingMfa`)

The following settings and properties are available from the CAS configuration catalog:

- `spring.application.name` (Third party; type `String`): Application name. Typically used with logging to help identify the application.
- `spring.profiles.active` (Third party; type `List<String>`): Comma-separated list of active profiles. Can be overridden by a command line switch.

Configuration files are loaded in the following order when the active profiles are `standalone,profile1,profile2`. Note that the last configuration file loaded will override any duplicate properties from configuration files loaded earlier:

1. `application.(properties|yml|yaml) `
2. (lower case) `<application name>.(properties|yml|yaml)`
3. `<application name>.(properties|yml|yaml)`
4. `application-standalone.(properties|yml|yaml)`
5. `standalone.(properties|yml|yaml)`
6. `application-profile1.(properties|yml|yaml)`
7. `profile1.(properties|yml|yaml)`
8. `application-profile2.(properties|yml|yaml)`
9. `profile2.(properties|yml|yaml)`

If two configuration files with same base name and different extensions exist, they are processed in the order of `properties`, `yml` and then `yaml` and then `groovy` (last one processed wins where duplicate properties exist). These external configuration files will override files located in the classpath (e.g. files from `src/main/resources` in your CAS overlay that end up in `WEB-INF/classes`) but the internal files are loaded per the [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/) rules which differ from the CAS standalone configuration rules described here (e.g. `<profile>.properties` would not be loaded from classpath but `application-<profile>.properties` would).

## Sources

CAS by default will attempt to locate settings and properties using:

1. `/etc/cas/config`
2. `/opt/cas/config`
3. `/var/cas/config`

CAS has the ability to also load a Groovy file for loading settings. The file is expected to be found at the above matching directory and should be named `${cas-application-name}.groovy`, such as `cas.groovy`. The script is able to combine conditional settings for active profiles and common settings that are applicable to all environments and profiles into one location with a structure that is similar to the below example:

```groovy
// Settings may be filtered by individual profiles
profiles {
    standalone {
        cas.some.setting="value"
    }
}

// This applies to all profiles and environments
cas.common.setting="value"
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

You can also use a dedicated configuration file to directly feed a collection of properties to CAS in form of a file or classpath resource. This is specially useful in cases where a bare CAS server is deployed in the cloud without the extra ceremony of a configuration server or an external directory for that matter and the deployer wishes to avoid overriding embedded configuration files.

The following settings and properties are available from the CAS configuration catalog:

- `cas.standalone.configuration-directory` (Optional; type `File`): Describes a directory path where CAS configuration may be found.
- `cas.standalone.configuration-file` (Optional; type `File`): Describes a file path where that contains the CAS properties in a single file.

## Handling Overrides

> **Remember**
>
> You are advised to not overlay or otherwise modify the built in `application.properties` or `bootstrap.properties` files. This will only complicate and weaken your deployment. Instead try to comply with the CAS defaults and bootstrap CAS as much as possible via the defaults, override via `application.yml`, `application-standalone.properties` or use the [outlined strategies](https://apereo.github.io/cas/development/configuration/Configuration-Management.html). Likewise, try to instruct CAS to locate configuration files external to its own. Premature optimization will only lead to chaos.

The following settings and properties are available from the CAS configuration catalog:

- `spring.cloud.config.allow-override` (Third party; type `Boolean`; default `true`): Flag to indicate that `#isOverrideSystemProperties() systemPropertiesOverride` can be used. Set to false to prevent users from changing the default accidentally. Default true.
- `spring.cloud.config.charset` (Third party; type `Charset`; default `UTF-8`): The charset to read the resource from the config server.
- `spring.cloud.config.enabled` (Third party; type `Boolean`; default `true`): Flag to say that remote configuration is enabled. Default true;
- `spring.cloud.config.fail-fast` (Third party; type `Boolean`; default `false`): Flag to indicate that failure to connect to the server is fatal (default false).
- `spring.cloud.config.headers` (Third party; type `Map<String,String>`): Additional headers used to create the client request.
- `spring.cloud.config.initialize-on-context-refresh` (Third party; type `Boolean`; default `false`): Flag to initialize bootstrap configuration on context refresh event. Default false.
- `spring.cloud.config.label` (Third party; type `String`): The label name to use to pull remote configuration properties. The default is set on the server (generally "main" for a git based server).
- `spring.cloud.config.media-type` (Third party; type `String`): The Accept header media type to send to config server.
- `spring.cloud.config.monitor.endpoint.path` (Third party; type `String`; deprecation `WARNING`, replaced by `spring.cloud.config.server.monitor.endpoint.path`)
- `spring.cloud.config.multiple-uri-strategy` (Third party; type `ConfigClientProperties.MultipleUriStrategy`; default `always`): The strategy to use when call to server fails and there are multiple URLs configured on the uri property (default `MultipleUriStrategy#ALWAYS`).
- `spring.cloud.config.name` (Third party; type `String`): Name of application used to fetch remote properties.
- `spring.cloud.config.oauth2.client-registration-id` (Third party; type `String`): Spring Security client registration id (as configured under `spring.security.oauth2.client.registration.<id>`) to use when fetching the access token.
- `spring.cloud.config.oauth2.enabled` (Third party; type `Boolean`; default `false`): Whether to attach an OAuth2 bearer token to outbound config-client requests.
- `spring.cloud.config.override-none` (Third party; type `Boolean`; default `false`): Flag to indicate that when `#setAllowOverride(boolean) allowOverride` is true, external properties should take lowest priority and should not override any existing property sources (including local config files). Default false. This will only have an effect when using config first bootstrap.
- `spring.cloud.config.override-system-properties` (Third party; type `Boolean`; default `true`): Flag to indicate that the external properties should override system properties. Default true.
- `spring.cloud.config.password` (Third party; type `String`): The password to use (HTTP Basic) when contacting the remote server.
- `spring.cloud.config.profile` (Third party; type `String`; default `default`): The default profile to use when fetching remote configuration (comma-separated). Default is "default".
- `spring.cloud.config.request-connect-timeout` (Third party; type `Integer`): timeout on waiting to connect to the Config Server.
- `spring.cloud.config.request-read-timeout` (Third party; type `Integer`): timeout on waiting to read data from the Config Server.
- `spring.cloud.config.resource-client.max-size` (Third party; type `DataSize`; default `1MB`): Maximum size of a file read from the config server; a larger file is rejected instead of being loaded into memory. Zero disables the limit.
- `spring.cloud.config.retry.enabled` (Third party; type `Boolean`; default `false`): Whether to retry failed requests to the Config Server. Only has an effect when spring.cloud.config.fail-fast is enabled.
- `spring.cloud.config.retry.initial-interval` (Third party; type `Long`; default `1000`): Initial retry interval in milliseconds.
- `spring.cloud.config.retry.max-attempts` (Third party; type `Integer`; default `6`): Maximum number of attempts.
- `spring.cloud.config.retry.max-interval` (Third party; type `Long`; default `2000`): Maximum interval for backoff.
- `spring.cloud.config.retry.multiplier` (Third party; type `Double`; default `1.1`): Multiplier for next interval.
- `spring.cloud.config.retry.use-random-policy` (Third party; type `Boolean`; deprecation `WARNING`, no replacement; default `false`): Use a random exponential backoff policy.
- `spring.cloud.config.send-all-labels` (Third party; type `Boolean`; default `false`): If set to true the client will send all labels to the server instead of sending one at a time. Support for this would require a config server version of 4.2.0 or higher.
- `spring.cloud.config.send-state` (Third party; type `Boolean`; default `true`): Flag to indicate whether to send state. Default true.
- `spring.cloud.config.server.accept-empty` (Third party; type `Boolean`; default `true`): Flag to indicate that If HTTP 404 needs to be sent if Application is not Found.
- `spring.cloud.config.server.aws-secretsmanager.default-label` (Third party; type `String`): The default staging label to be used to fetch the secret values. If unset, an active version of the secret will be fetched (AWSCURRENT).
- `spring.cloud.config.server.aws-secretsmanager.endpoint` (Third party; type `String`): The endpoint to be used by AWS Secrets Manager client. This can be used to specify an alternate endpoint for the API requests.
- `spring.cloud.config.server.aws-secretsmanager.ignore-label` (Third party; type `Boolean`; default `false`): Do not set staging label when fetching the secret values.
- `spring.cloud.config.server.aws-secretsmanager.order` (Third party; type `Integer`): The order of the environment repository.
- `spring.cloud.config.server.aws-secretsmanager.origin` (Third party; type `String`; default `aws:secrets:`): Prefix which indicates the origin of the property. Defaults to "aws:secrets:".
- `spring.cloud.config.server.aws-secretsmanager.prefix` (Third party; type `String`): Prefix indicating first level for every property loaded from AWS Secrets Manager. Value must start with a forward slash followed by a valid path segment or be empty. Defaults to "/secret".
- `spring.cloud.config.server.aws-secretsmanager.profile-separator` (Third party; type `String`; default `-`): String that separates profile from the application name.
- `spring.cloud.config.server.aws-secretsmanager.region` (Third party; type `String`): The region to be used by AWS Secrets Manager client.
- `spring.cloud.config.server.awsparamstore.decrypt-values` (Third party; type `Boolean`; default `true`): Flag to indicate the retrieval of all AWS parameters in a hierarchy with their value decrypted. Defaults to "true".
- `spring.cloud.config.server.awsparamstore.endpoint` (Third party; type `String`): The service endpoint to be used by the AWS Parameter Store client.
- `spring.cloud.config.server.awsparamstore.max-results` (Third party; type `Integer`; default `10`): The maximum number of items to return for an AWS Parameter Store API call. Defaults to "10".
- `spring.cloud.config.server.awsparamstore.order` (Third party; type `Integer`): The order of the environment repository.
- `spring.cloud.config.server.awsparamstore.origin` (Third party; type `String`; default `aws:ssm:parameter:`): Prefix indicating the property's origin. Defaults to "aws:ssm:parameter:".
- `spring.cloud.config.server.awsparamstore.prefix` (Third party; type `String`): Prefix indicating first level for every property loaded from the AWS Parameter Store. Value must start with a forward slash followed by one or more valid path segments or be empty. Defaults to "/config".
- `spring.cloud.config.server.awsparamstore.profile-separator` (Third party; type `String`; default `-`): String that separates an appended profile from the context name. Note that an AWS parameter name can only contain dots, dashes and underscores next to alphanumeric characters. Defaults to "-".
- `spring.cloud.config.server.awsparamstore.recursive` (Third party; type `Boolean`; default `true`): Flag to indicate the retrieval of all AWS parameters within a hierarchy. Defaults to "true".
- `spring.cloud.config.server.awsparamstore.region` (Third party; type `String`): The region to be used by the AWS Parameter Store client.
- `spring.cloud.config.server.awss3.bucket` (Third party; type `String`): Name of the S3 bucket that contains config.
- `spring.cloud.config.server.awss3.endpoint` (Third party; type `String`): Adds the ability to override the baseUrl of the s3 client.
- `spring.cloud.config.server.awss3.order` (Third party; type `Integer`)
- `spring.cloud.config.server.awss3.region` (Third party; type `String`): AWS region that contains config.
- `spring.cloud.config.server.awss3.search-paths` (Third party; type `List<String>`): List of directory paths to search for profiles in the bucket. Analogous to `#searchPaths` in Git backend.
- `spring.cloud.config.server.awss3.use-directory-layout` (Third party; type `Boolean`; default `false`): Use application name as intermediate directory. Analogous to `#searchPaths` from Git backend.
- `spring.cloud.config.server.bootstrap` (Third party; type `Boolean`; default `false`): Flag indicating that the config server should initialize its own Environment with properties from the remote repository. Off by default because it delays startup but can be useful when embedding the server in another application.
- `spring.cloud.config.server.credhub.ca-cert-files` (Third party; type `String[]`)
- `spring.cloud.config.server.credhub.connection-timeout` (Third party; type `Duration`)
- `spring.cloud.config.server.credhub.default-label` (Third party; type `String`; default `master`): The default label to be used when is not provided by client applications.
- `spring.cloud.config.server.credhub.oauth2.registration-id` (Third party; type `String`)
- `spring.cloud.config.server.credhub.order` (Third party; type `Integer`)
- `spring.cloud.config.server.credhub.path` (Third party; type `String`): The common base path for credentials in CredHub. It is empty by default.
- `spring.cloud.config.server.credhub.read-timeout` (Third party; type `Duration`)
- `spring.cloud.config.server.credhub.url` (Third party; type `String`)
- `spring.cloud.config.server.default-application-name` (Third party; type `String`; default `application`): Default application name when incoming requests do not have a specific one.
- `spring.cloud.config.server.default-label` (Third party; type `String`): Default repository label when incoming requests do not have a specific label.
- `spring.cloud.config.server.default-profile` (Third party; type `String`; default `default`): Default application profile when incoming requests do not have a specific one.
- `spring.cloud.config.server.enabled` (Third party; type `Boolean`; default `true`): Flag indicating config server is enabled.
- `spring.cloud.config.server.encrypt.enabled` (Third party; type `Boolean`; default `true`): Enable decryption of environment properties before sending to client.
- `spring.cloud.config.server.encrypt.plain-text-encrypt` (Third party; type `Boolean`; default `false`): Enable decryption of environment properties served by plain text endpoint `org.springframework.cloud.config.server.resource.ResourceController`.
- `spring.cloud.config.server.fail-on-composite-error` (Third party; type `Boolean`; default `true`): Flag indicating that if there are any errors reading properties from a subordinate environment repository in a composite environment repository, then the entire composite read should fail. Useful when set to false when a Vault repository is in the composite to allow clients to still read properties from other repositories without providing a valid Vault token. Defaults to true, resulting in a failure on any error.
- `spring.cloud.config.server.gcp-parameter-manager.application-label` (Third party; type `String`; default `application`)
- `spring.cloud.config.server.gcp-parameter-manager.default-label` (Third party; type `String`; default `main`)
- `spring.cloud.config.server.gcp-parameter-manager.location` (Third party; type `String`; default `global`)
- `spring.cloud.config.server.gcp-parameter-manager.order` (Third party; type `Integer`)
- `spring.cloud.config.server.gcp-parameter-manager.profile-label` (Third party; type `String`; default `profile`)
- `spring.cloud.config.server.gcp-parameter-manager.project-id` (Third party; type `String`)
- `spring.cloud.config.server.gcp-secret-manager.allowed-project-ids` (Third party; type `List<String>`): When `token-mandatory` is `false`: only these project IDs may be used with client-supplied `X-Project-ID`. When empty, client-supplied project IDs are rejected. When `token-mandatory` is `true`, this list is ignored for the header; use `X-Config-Token` permission checks instead.
- `spring.cloud.config.server.gcp-secret-manager.application-label` (Third party; type `String`; default `application`)
- `spring.cloud.config.server.gcp-secret-manager.order` (Third party; type `Integer`)
- `spring.cloud.config.server.gcp-secret-manager.profile-label` (Third party; type `String`; default `profile`)
- `spring.cloud.config.server.gcp-secret-manager.project-id` (Third party; type `String`): Used when `X-Project-ID` is absent and the metadata server is unavailable (for example local development). Not validated against `#allowedProjectIds`.
- `spring.cloud.config.server.gcp-secret-manager.service-account` (Third party; type `String`)
- `spring.cloud.config.server.gcp-secret-manager.token-mandatory` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.gcp-secret-manager.version` (Third party; type `Integer`; default `1`)
- `spring.cloud.config.server.gcs.bucket` (Third party; type `String`)
- `spring.cloud.config.server.gcs.order` (Third party; type `Integer`)
- `spring.cloud.config.server.gcs.project-id` (Third party; type `String`)
- `spring.cloud.config.server.gcs.search-paths` (Third party; type `List<String>`)
- `spring.cloud.config.server.gcs.use-directory-layout` (Third party; type `Boolean`; default `false`)
- `spring.cloud.config.server.git.azure.identity.client-id` (Third party; type `String`): Client ID of the Azure managed identity used for Azure DevOps authentication.
- `spring.cloud.config.server.git.azure.identity.managed-identity-enabled` (Third party; type `Boolean`; default `false`): Whether managed identity authentication is enabled for Azure DevOps repositories.
- `spring.cloud.config.server.git.basedir` (Third party; type `File`): Base directory for local working copy of repository.
- `spring.cloud.config.server.git.clone-on-start` (Third party; type `Boolean`; default `false`): Flag to indicate that the repository should be cloned on startup (not on demand). Generally leads to slower startup but faster first query.
- `spring.cloud.config.server.git.clone-submodules` (Third party; type `Boolean`; default `false`): Flag to indicate that the submodules in the repository should be cloned.
- `spring.cloud.config.server.git.continue-on-multiple-label-failure` (Third party; type `Boolean`; default `false`): Flag to indicate whether to continue on multiple label failure. Defaults to false.
- `spring.cloud.config.server.git.default-label` (Third party; type `String`): The default label to be used with the remote repository.
- `spring.cloud.config.server.git.delete-untracked-branches` (Third party; type `Boolean`; default `false`): Flag to indicate that the branch should be deleted locally if it's origin tracked branch was removed.
- `spring.cloud.config.server.git.force-pull` (Third party; type `Boolean`; default `false`): Flag to indicate that the repository should force pull. If true discard any local changes and take from remote repository.
- `spring.cloud.config.server.git.host-key` (Third party; type `String`): Valid SSH host key. Must be set if hostKeyAlgorithm is also set.
- `spring.cloud.config.server.git.host-key-algorithm` (Third party; type `String`): One of ssh-dss, ssh-rsa, ssh-ed25519, ecdsa-sha2-nistp256, ecdsa-sha2-nistp384, or ecdsa-sha2-nistp521. Must be set if hostKey is also set.
- `spring.cloud.config.server.git.ignore-local-ssh-settings` (Third party; type `Boolean`; default `false`): If true, use property-based instead of file-based SSH config.
- `spring.cloud.config.server.git.known-hosts-file` (Third party; type `String`): Location of custom .known_hosts file.
- `spring.cloud.config.server.git.order` (Third party; type `Integer`): The order of the environment repository.
- `spring.cloud.config.server.git.passphrase` (Third party; type `String`): Passphrase for unlocking your ssh private key.
- `spring.cloud.config.server.git.password` (Third party; type `String`): Password for authentication with remote repository.
- `spring.cloud.config.server.git.preferred-authentications` (Third party; type `String`): Override server authentication method order. This should allow for evading login prompts if server has keyboard-interactive authentication before the publickey method.
- `spring.cloud.config.server.git.private-key` (Third party; type `String`): Valid SSH private key. Must be set if ignoreLocalSshSettings is true and Git URI is SSH format.
- `spring.cloud.config.server.git.proxy` (Third party; type `Map<ProxyHostProperties.ProxyForScheme,ProxyHostProperties>`): HTTP proxy configuration.
- `spring.cloud.config.server.git.refresh-rate` (Third party; type `Integer`; default `0`): Time (in seconds) between refresh of the git repository.
- `spring.cloud.config.server.git.repos` (Third party; type `Map<String,MultipleJGitEnvironmentProperties.PatternMatchingJGitEnvironmentProperties>`): Map of repository identifier to location and other properties.
- `spring.cloud.config.server.git.search-paths` (Third party; type `String[]`): Search paths to use within local working copy. By default searches only the root.
- `spring.cloud.config.server.git.skip-ssl-validation` (Third party; type `Boolean`; default `false`): Flag to indicate that SSL certificate validation should be bypassed when communicating with a repository served over an HTTPS connection.
- `spring.cloud.config.server.git.strict-host-key-checking` (Third party; type `Boolean`; default `true`): If false, ignore errors with host key.
- `spring.cloud.config.server.git.timeout` (Third party; type `Integer`; default `5`): Timeout (in seconds) for obtaining HTTP or SSH connection (if applicable), defaults to 5 seconds.
- `spring.cloud.config.server.git.try-master-branch` (Third party; type `Boolean`; default `true`): To maintain compatibility we will try the master branch in addition to main when we try to fetch the default branch.
- `spring.cloud.config.server.git.uri` (Third party; type `String`): URI of remote repository.
- `spring.cloud.config.server.git.username` (Third party; type `String`): Username for authentication with remote repository.
- `spring.cloud.config.server.health.accept-empty` (Third party; type `Boolean`; default `false`)
- `spring.cloud.config.server.health.down-health-status` (Third party; type `String`)
- `spring.cloud.config.server.health.repositories` (Third party; type `Map<String,ConfigServerHealthIndicator.Repository>`)
- `spring.cloud.config.server.ignore-unresolvable-nested-placeholders` (Third party; type `Boolean`; default `false`): Flag indicating that unresolvable nested placeholders should be ignored when resolving placeholders for YAML and JSON responses.
- `spring.cloud.config.server.jdbc.default-label` (Third party; type `String`; default `master`)
- `spring.cloud.config.server.jdbc.enabled` (Third party; type `Boolean`; default `true`): Flag to indicate that JDBC environment repository configuration is enabled.
- `spring.cloud.config.server.jdbc.fail-on-error` (Third party; type `Boolean`; default `true`): Flag to determine how to handle query exceptions.
- `spring.cloud.config.server.jdbc.order` (Third party; type `Integer`)
- `spring.cloud.config.server.jdbc.sql` (Third party; type `String`; default `SELECT "KEY", "VALUE" from PROPERTIES where APPLICATION=? and PROFILE=? and LABEL=?`): SQL used to query database for keys and values.
- `spring.cloud.config.server.jdbc.sql-without-profile` (Third party; type `String`; default `SELECT "KEY", "VALUE" from PROPERTIES where APPLICATION=? and PROFILE is null and LABEL=?`): SQL used to query database for keys and values when profile is null.
- `spring.cloud.config.server.mongodb.collection` (Third party; type `String`; default `properties`): Name of the MongoDB collection to query for configuration properties.
- `spring.cloud.config.server.mongodb.default-label` (Third party; type `String`; default `master`): Default label to use if none is specified.
- `spring.cloud.config.server.mongodb.enabled` (Third party; type `Boolean`; default `true`): Flag to indicate that MongoDB environment repository configuration is enabled.
- `spring.cloud.config.server.mongodb.fail-on-error` (Third party; type `Boolean`; default `true`): Flag to determine how to handle query exceptions.
- `spring.cloud.config.server.mongodb.order` (Third party; type `Integer`): Order of the MongoDB environment repository.
- `spring.cloud.config.server.monitor.bitbucket.enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.bitbucket.validation-enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.bitbucket.webhook-secret` (Third party; type `String`)
- `spring.cloud.config.server.monitor.endpoint.path` (Third party; type `String`; default `/monitor`)
- `spring.cloud.config.server.monitor.gitea.enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gitea.validation-enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gitea.webhook-secret` (Third party; type `String`)
- `spring.cloud.config.server.monitor.gitee.enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gitee.validation-enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gitee.webhook-secret` (Third party; type `String`)
- `spring.cloud.config.server.monitor.github.enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.github.validation-enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.github.webhook-secret` (Third party; type `String`)
- `spring.cloud.config.server.monitor.gitlab.enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gitlab.validation-enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gitlab.webhook-secret` (Third party; type `String`)
- `spring.cloud.config.server.monitor.gogs.enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gogs.validation-enabled` (Third party; type `Boolean`; default `true`)
- `spring.cloud.config.server.monitor.gogs.webhook-secret` (Third party; type `String`)
- `spring.cloud.config.server.monitor.http.endpoints` (Third party; type `Map<String,String>`)
- `spring.cloud.config.server.monitor.ignored-paths` (Third party; type `List<String>`)
- `spring.cloud.config.server.monitor.max-dashes` (Third party; type `Integer`; default `20`): Upper bound on the number of dash-separated segments processed when guessing a service name from a notification path. Prevents a long, heavily dashed path from producing an unbounded number of candidate service names (and refresh events).
- `spring.cloud.config.server.monitor.max-paths` (Third party; type `Integer`; default `20`)
- `spring.cloud.config.server.native.add-label-locations` (Third party; type `Boolean`; default `true`): Flag to determine whether label locations should be added.
- `spring.cloud.config.server.native.default-label` (Third party; type `String`; default `master`)
- `spring.cloud.config.server.native.fail-on-error` (Third party; type `Boolean`; default `false`): Flag to determine how to handle exceptions during decryption (default false).
- `spring.cloud.config.server.native.last-label-wins` (Third party; type `Boolean`; default `true`): Flag to determine whether, when several labels are requested, the last label takes precedence over the earlier ones (default true). If false, the first label takes precedence, as it did before this property was introduced.
- `spring.cloud.config.server.native.order` (Third party; type `Integer`)
- `spring.cloud.config.server.native.search-locations` (Third party; type `String[]`): Locations to search for configuration files. Defaults to the same as a Spring Boot app so [classpath:/,classpath:/config/,file:./,file:./config/].
- `spring.cloud.config.server.native.version` (Third party; type `String`): Version string to be reported for native repository.
- `spring.cloud.config.server.overrides` (Third party; type `Map<String,String>`): Extra map for a property source to be sent to all clients unconditionally.
- `spring.cloud.config.server.prefix` (Third party; type `String`): Prefix for configuration resource paths (default is empty). Useful when embedding in another application when you don't want to change the context path or servlet path.
- `spring.cloud.config.server.redis.order` (Third party; type `Integer`)
- `spring.cloud.config.server.reverse-location-order` (Third party; type `Boolean`; default `false`): By default the location order we use in GenericResourceRepository is the order in which they are listed. Prior to Hoxton.SR11 the order used to be reverse. If this property is set to true then we will reverse the order like it used to be prior to Hoxton.SR11.
- `spring.cloud.config.server.strip-document-from-yaml` (Third party; type `Boolean`; default `true`): Flag to indicate that YAML documents that are text or collections (not a map) should be returned in "native" form.
- `spring.cloud.config.server.svn.basedir` (Third party; type `File`): Base directory for local working copy of repository.
- `spring.cloud.config.server.svn.continue-on-multiple-label-failure` (Third party; type `Boolean`; default `false`): Flag to indicate whether to continue on multiple label failure. Defaults to false.
- `spring.cloud.config.server.svn.default-label` (Third party; type `String`): The default label to be used with the remote repository.
- `spring.cloud.config.server.svn.order` (Third party; type `Integer`): The order of the environment repository.
- `spring.cloud.config.server.svn.passphrase` (Third party; type `String`): Passphrase for unlocking your ssh private key.
- `spring.cloud.config.server.svn.password` (Third party; type `String`): Password for authentication with remote repository.
- `spring.cloud.config.server.svn.search-paths` (Third party; type `String[]`): Search paths to use within local working copy. By default searches only the root.
- `spring.cloud.config.server.svn.strict-host-key-checking` (Third party; type `Boolean`; default `true`): Reject incoming SSH host keys from remote servers not in the known host list.
- `spring.cloud.config.server.svn.uri` (Third party; type `String`): URI of remote repository.
- `spring.cloud.config.server.svn.username` (Third party; type `String`): Username for authentication with remote repository.
- `spring.cloud.config.server.vault.app-role.app-role-path` (Third party; type `String`; default `approle`): Mount path of the AppRole authentication backend.
- `spring.cloud.config.server.vault.app-role.role` (Third party; type `String`): Name of the role, optional, used for pull-mode.
- `spring.cloud.config.server.vault.app-role.role-id` (Third party; type `String`): The RoleId.
- `spring.cloud.config.server.vault.app-role.secret-id` (Third party; type `String`): The SecretId.
- `spring.cloud.config.server.vault.authentication` (Third party; type `VaultEnvironmentProperties.AuthenticationMethod`)
- `spring.cloud.config.server.vault.aws-ec2.aws-ec2-path` (Third party; type `String`; default `aws-ec2`): Mount path of the AWS-EC2 authentication backend.
- `spring.cloud.config.server.vault.aws-ec2.identity-document` (Third party; type `String`; default `http://169.254.169.254/latest/dynamic/instance-identity/pkcs7`): URL of the AWS-EC2 PKCS7 identity document.
- `spring.cloud.config.server.vault.aws-ec2.nonce` (Third party; type `String`): Nonce used for AWS-EC2 authentication. An empty nonce defaults to nonce generation.
- `spring.cloud.config.server.vault.aws-ec2.role` (Third party; type `String`): Name of the role, optional.
- `spring.cloud.config.server.vault.aws-iam.aws-path` (Third party; type `String`; default `aws`): Mount path of the AWS authentication backend.
- `spring.cloud.config.server.vault.aws-iam.endpoint-uri` (Third party; type `URI`): STS server URI. @since 2.2
- `spring.cloud.config.server.vault.aws-iam.role` (Third party; type `String`): Name of the role, optional. Defaults to the friendly IAM name if not set.
- `spring.cloud.config.server.vault.aws-iam.server-name` (Third party; type `String`): Name of the server used to set `X-Vault-AWS-IAM-Server-ID` header in the headers of login requests.
- `spring.cloud.config.server.vault.azure-msi.azure-path` (Third party; type `String`; default `azure`): Mount path of the Azure MSI authentication backend.
- `spring.cloud.config.server.vault.azure-msi.identity-token-service` (Third party; type `String`): URI to the Azure MSI Identity Service.
- `spring.cloud.config.server.vault.azure-msi.metadata-service` (Third party; type `String`): URI to the Azure MSI Metadata Service.
- `spring.cloud.config.server.vault.azure-msi.role` (Third party; type `String`): Name of the role.
- `spring.cloud.config.server.vault.backend` (Third party; type `String`; default `secret`): Vault backend. Defaults to secret.
- `spring.cloud.config.server.vault.default-key` (Third party; type `String`; default `application`): The key in vault shared by all applications. Defaults to application. Set to empty to disable.
- `spring.cloud.config.server.vault.default-label` (Third party; type `String`; default `main`)
- `spring.cloud.config.server.vault.enable-label` (Third party; type `Boolean`; default `false`): Flag to indicate that the repository should use 'label' as well as 'application-name' and 'profile', for vault secrets. By default, the vault secrets are expected to be in 'application-name,profile' path. When this flag enabled, they are expected to be in 'application-name,profile,label' path. To maintain compatibility this flag is not enabled by default.
- `spring.cloud.config.server.vault.full-key-path` (Third party; type `Boolean`; default `false`): Include the full key path in the PropertySource name. Defaults to false
- `spring.cloud.config.server.vault.gcp-gce.gcp-path` (Third party; type `String`; default `gcp`): Mount path of the Kubernetes authentication backend.
- `spring.cloud.config.server.vault.gcp-gce.role` (Third party; type `String`): Name of the role against which the login is being attempted.
- `spring.cloud.config.server.vault.gcp-gce.service-account` (Third party; type `String`): Optional service account id. Using the default id if left unconfigured.
- `spring.cloud.config.server.vault.gcp-iam.credentials.encoded-key` (Third party; type `String`): The base64 encoded contents of an OAuth2 account private key in JSON format.
- `spring.cloud.config.server.vault.gcp-iam.credentials.location` (Third party; type `Resource`): Location of the OAuth2 credentials private key. Since this is a Resource, the private key can be in a multitude of locations, such as a local file system, classpath, URL, etc.
- `spring.cloud.config.server.vault.gcp-iam.gcp-path` (Third party; type `String`; default `gcp`): Mount path of the Kubernetes authentication backend.
- `spring.cloud.config.server.vault.gcp-iam.jwt-validity` (Third party; type `Duration`; default `15m`): Validity of the JWT token.
- `spring.cloud.config.server.vault.gcp-iam.project-id` (Third party; type `String`): Overrides the GCP project Id.
- `spring.cloud.config.server.vault.gcp-iam.role` (Third party; type `String`): Name of the role against which the login is being attempted.
- `spring.cloud.config.server.vault.gcp-iam.service-account-id` (Third party; type `String`): Overrides the GCP service account Id.
- `spring.cloud.config.server.vault.host` (Third party; type `String`; default `127.0.0.1`): Vault host. Defaults to 127.0.0.1.
- `spring.cloud.config.server.vault.kubernetes.kubernetes-path` (Third party; type `String`; default `kubernetes`): Mount path of the Kubernetes authentication backend.
- `spring.cloud.config.server.vault.kubernetes.role` (Third party; type `String`): Name of the role against which the login is being attempted.
- `spring.cloud.config.server.vault.kubernetes.service-account-token-file` (Third party; type `String`; default `/var/run/secrets/kubernetes.io/serviceaccount/token`): Path to the service account token file.
- `spring.cloud.config.server.vault.kv-version` (Third party; type `Integer`; default `1`): Value to indicate which version of Vault kv backend is used. Defaults to 1.
- `spring.cloud.config.server.vault.namespace` (Third party; type `String`): The value of the Vault X-Vault-Namespace header. Defaults to null. This a Vault Enterprise feature only.
- `spring.cloud.config.server.vault.order` (Third party; type `Integer`)
- `spring.cloud.config.server.vault.path-to-key` (Third party; type `String`): KV2 API required "data" after "mount-path". There could be folder/path structure, where the keys/applications are grouped. This property is the path after mount-path, under which application(s) are located (appended after "data") Default value is blank, which means all grouped applications are located right under the mount-path
- `spring.cloud.config.server.vault.pcf.instance-certificate` (Third party; type `Resource`): Path to the instance certificate (PEM). Defaults to `CF_INSTANCE_CERT` env variable.
- `spring.cloud.config.server.vault.pcf.instance-key` (Third party; type `Resource`): Path to the instance key (PEM). Defaults to `CF_INSTANCE_KEY` env variable.
- `spring.cloud.config.server.vault.pcf.pcf-path` (Third party; type `String`; default `pcf`): Mount path of the Kubernetes authentication backend.
- `spring.cloud.config.server.vault.pcf.role` (Third party; type `String`): Name of the role against which the login is being attempted.
- `spring.cloud.config.server.vault.port` (Third party; type `Integer`; default `8200`): Vault port. Defaults to 8200.
- `spring.cloud.config.server.vault.profile-separator` (Third party; type `String`; default `,`): Vault profile separator. Defaults to comma.
- `spring.cloud.config.server.vault.proxy` (Third party; type `Map<ProxyHostProperties.ProxyForScheme,ProxyHostProperties>`): HTTP proxy configuration.
- `spring.cloud.config.server.vault.scheme` (Third party; type `String`; default `http`): Vault scheme. Defaults to http.
- `spring.cloud.config.server.vault.skip-ssl-validation` (Third party; type `Boolean`; default `false`): Flag to indicate that SSL certificate validation should be bypassed when communicating with a repository served over an HTTPS connection.
- `spring.cloud.config.server.vault.ssl.cert-auth-path` (Third party; type `String`; default `cert`): Mount path of the TLS cert authentication backend.
- `spring.cloud.config.server.vault.ssl.key-store` (Third party; type `Resource`): Trust store that holds certificates and private keys.
- `spring.cloud.config.server.vault.ssl.key-store-password` (Third party; type `String`): Password used to access the key store.
- `spring.cloud.config.server.vault.ssl.trust-store` (Third party; type `Resource`): Trust store that holds SSL certificates.
- `spring.cloud.config.server.vault.ssl.trust-store-password` (Third party; type `String`): Password used to access the trust store.
- `spring.cloud.config.server.vault.timeout` (Third party; type `Integer`; default `5`): Timeout (in seconds) for obtaining HTTP connection, defaults to 5 seconds.
- `spring.cloud.config.server.vault.token` (Third party; type `String`): Static vault token. Required if `#authentication` is `TOKEN`.
- `spring.cloud.config.tls` (Third party; type `TlsProperties`): TLS properties.
- `spring.cloud.config.token` (Third party; type `String`): Security Token passed thru to underlying environment repository.
- `spring.cloud.config.uri` (Third party; type `String[]`; default `["http://localhost:8888"]`): The URI of the remote server (default http://localhost:8888).
- `spring.cloud.config.username` (Third party; type `String`): The username to use (HTTP Basic) when contacting the remote server.

**Hibernate & JDBC**

Control global properties that are relevant to Hibernate, when CAS attempts to employ and utilize database resources, connections and queries.

- `cas.jdbc.gen-ddl` (Optional; type `Boolean`; default `true`): Whether to generate DDL after the EntityManagerFactory has been initialized creating/updating all relevant tables.
- `cas.jdbc.physical-table-names` (Optional; type `Map<String,String>`): Indicate a physical table name to be used by the hibernate naming strategy in case table names need to be customized for the specific type of database. The key here indicates the CAS-provided table name and the value is the translate physical name for the database. If a match is not found for the CAS-provided table name, then that name will be used by default.
- `cas.jdbc.show-sql` (Optional; type `Boolean`; default `false`): Whether SQL queries should be displayed in the console/logs.
