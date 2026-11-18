# Spring Cloud Configuration Server - Spring Cloud Native

Source: https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-Native.html

Spring Cloud Configuration Server is configured by default to load `cas.(properties|yml)` files from an external location that is `/etc/cas/config`. This location is constantly monitored by the server to detect external changes. Note that this location needs to exist, and does not require any special permissions or structure. The name of the configuration file that goes inside this directory needs to match the application name (i.e. `cas.properties`).

The following settings and properties are available from the CAS configuration catalog:

- `spring.cloud.config.server.native.add-label-locations` (Third party; type `Boolean`; default `true`): Flag to determine whether label locations should be added.
- `spring.cloud.config.server.native.default-label` (Third party; type `String`; default `master`)
- `spring.cloud.config.server.native.fail-on-error` (Third party; type `Boolean`; default `false`): Flag to determine how to handle exceptions during decryption (default false).
- `spring.cloud.config.server.native.last-label-wins` (Third party; type `Boolean`; default `true`): Flag to determine whether, when several labels are requested, the last label takes precedence over the earlier ones (default true). If false, the first label takes precedence, as it did before this property was introduced.
- `spring.cloud.config.server.native.order` (Third party; type `Integer`)
- `spring.cloud.config.server.native.search-locations` (Third party; type `String[]`): Locations to search for configuration files. Defaults to the same as a Spring Boot app so [classpath:/,classpath:/config/,file:./,file:./config/].
- `spring.cloud.config.server.native.version` (Third party; type `String`): Version string to be reported for native repository.
- `spring.profiles.active` (Third party; type `List<String>`): Comma-separated list of active profiles. Can be overridden by a command line switch.
- `spring.profiles.include` (Third party; type `List<String>`): Unconditionally activate the specified comma-separated list of profiles (or list of profiles if using YAML).

If you want to use additional configuration files, they need to have the form `application-<profile>.(properties|yml)`. A file named `application.(properties|yml)` will be included by default. The profile specific files are activated by including their profiles, such as `profile1,profile2`, in the `src/main/resources/bootstrap.properties` file, where the `native` profile is active and the search locations of the server point at `file:///etc/cas/config`.

An external `.properties` file hosted by an external location holds CAS settings as usual. You could have just as well used a `cas.yml` file to host the changes. Note that the default profile here is the `native` profile.
