# Spring Cloud Configuration Server - Spring Cloud Default

Source: https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-Default.html

The Spring Cloud Configuration Server is able to handle `git` or `svn` based repositories that host CAS configuration. Such repositories can either be local to the deployment, or they could be on the cloud in form of GitHub/Bitbucket. Access to cloud-based repositories can either be in form of a username/password, or via SSH so as long the appropriate keys are configured in the CAS deployment environment which is really no different than how one would normally access a git repository via SSH.

The following settings and properties are available from the CAS configuration catalog:

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

> **Usage**
>
> The configuration modules provided here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.

Needless to say, the repositories could use both YAML and properties syntax to host configuration files. The default profile is activated by making `default` the active profile.

> **Keep What You Need!**
>
> Again, in all of the above strategies, an adopter is encouraged to only keep and maintain properties needed for their particular deployment. It is UNNECESSARY to grab a copy of all CAS settings and move them to an external location. Settings that are defined by the external configuration location or repository are able to override what is provided by CAS as a default.

Load settings from external properties/yaml configuration files.

The following settings and properties are available from the CAS configuration catalog:

- `spring.cloud.config.server.default-application-name` (Third party; type `String`; default `application`): Default application name when incoming requests do not have a specific one.
- `spring.cloud.config.server.default-label` (Third party; type `String`): Default repository label when incoming requests do not have a specific label.
- `spring.cloud.config.server.default-profile` (Third party; type `String`; default `default`): Default application profile when incoming requests do not have a specific one.
- `spring.profiles.active` (Third party; type `List<String>`): Comma-separated list of active profiles. Can be overridden by a command line switch.

## Git Repository

Allow the CAS Spring Cloud configuration server to load settings from an internal/external Git repository. This then allows CAS to become a client of the configuration server, consuming settings over HTTP where needed.

The following settings and properties are available from the CAS configuration catalog:

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
- `spring.profiles.active` (Third party; type `List<String>`): Comma-separated list of active profiles. Can be overridden by a command line switch.

The above configuration also applies to online git-based repositories such as GitHub, Bitbucket, etc.
