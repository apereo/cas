# Git Service Registry

Source: https://apereo.github.io/cas/development/services/Git-Service-Management.html

This registry reads services definitions from remote or local git repositories. Service definition files are expected to be either [JSON](https://apereo.github.io/cas/development/services/JSON-Service-Management.html) or [YAML](https://apereo.github.io/cas/development/services/YAML-Service-Management.html) files. The contents of the repository is pulled at defined intervals and changes to service definitions are committed and pushed to the defined remotes.

Support is enabled by adding the following module into the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-git-service-registry"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.service-registry.git.active-branch` (Required; type `String`; default `master`): The branch to checkout and activate, defaults to `master`.
- `cas.service-registry.git.branches-to-clone` (Required; type `String`; default `*`): If the repository is to be cloned, this will allow a select list of branches to be fetched. List the branch names separated by commas or use `*` to clone all branches. Defaults to all branches.
- `cas.service-registry.git.clone-directory.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.service-registry.git.private-key.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.service-registry.git.repository-url` (Required; type `String`): The address of the git repository. Could be a URL or a file-system path.
- `cas.service-registry.git.clear-existing-identities` (Optional; type `Boolean`; default `false`): When establishing an ssh session, determine if default identities loaded on the machine should be excluded/removed and identity should only be limited to those loaded from given keys.
- `cas.service-registry.git.group-by-type` (Optional; type `Boolean`; default `true`): Determine whether service definitions in the git repository should be located/stored in groups and separate folder structures based on the service type. @see #getRootDirectory()
- `cas.service-registry.git.http-client-type` (Optional; type `BaseGitProperties.HttpClientTypes`; default `JDK`): Implementation of HTTP client to use when doing git operations via http/https. The jgit library sets the connection factory statically (globally) so this property should be set to the same value for all git repositories (services, saml, etc). Not doing so might result in one connection factory being used for clone and another for subsequent fetches. Available values are as follows: - `JDK`: Built-in JDK http/https client. - `HTTP_CLIENT`: Apache HTTP Client http/https client.
- `cas.service-registry.git.order` (Optional; type `Integer`)
- `cas.service-registry.git.password` (Optional; type `String`): Password used to access or push to the repository.
- `cas.service-registry.git.private-key-passphrase` (Optional; type `String`): Password for the SSH private key.
- `cas.service-registry.git.push-changes` (Optional; type `Boolean`; default `false`): Decide whether changes should be pushed back into the remote repository.
- `cas.service-registry.git.rebase` (Optional; type `Boolean`; default `false`): Whether to rebase on pulls.
- `cas.service-registry.git.root-directory` (Optional; type `String`): Root directory in the git repository structure to track service definition files. This might be most useful if the git repository is tasked with other types of files and configurations and allowing a separate root directory for service definitions provide a clean separation between services files and everything else. This setting may work in concert with `#isGroupByType()`. If left blank, the root folder of the git repository itself is used as the root directory for service definitions.
- `cas.service-registry.git.sign-commits` (Optional; type `Boolean`; default `false`): Whether commits should be signed.
- `cas.service-registry.git.ssh-session-password` (Optional; type `String`): As with using SSH with public keys, an SSH session with `ssh://user@example.com/repo.git` must be specified to use password-secured SSH connections.
- `cas.service-registry.git.strict-host-key-checking` (Optional; type `Boolean`; default `true`): Whether on not to turn on strict host key checking. true will be "yes", false will be "no", "ask" not supported.
- `cas.service-registry.git.timeout` (Optional; type `String`; default `PT10S`): Timeout for git operations such as push and pull in seconds.
- `cas.service-registry.git.username` (Optional; type `String`): Username used to access or push to the repository.

> **No Interference!**
>
> Be careful to not manually modify the state of the git repository directory that is cloned on the local server. By doing so, you risk interfering with CAS' own service management processes and ultimately may end up corrupting the state of the git repository.

The service registry is also able to auto detect changes as it will pull changes from defined remotes periodically. It will monitor changes to recognize file additions, removals and updates and will auto-refresh CAS so changes may happen instantly.

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="org.eclipse.jgit" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](https://apereo.github.io/cas/development/services/AutoInitialization-Service-Management.html) for more info.
