# Spring Cloud Configuration Server - GCP Secret Manager

Source: https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-GCP-SecretManager.html

CAS is also able to use [Google Cloud Secret Manager](https://cloud.google.com/security/products/secret-manager) to locate properties and settings. This functionality is delivered to CAS via [Spring Cloud GCP](https://github.com/GoogleCloudPlatform/spring-cloud-gcp).

Support is provided via the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-configuration-cloud-gcp-secretsmanager"
}
```

> **Usage**
>
> The configuration modules provided here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.

The following settings and properties are available from the CAS configuration catalog:

- `spring.cloud.gcp.secretmanager.allow-default-secret` (Third party; type `Boolean`; default `false`): Whether the secret manager will allow a default secret value when accessing a non-existing secret. When set to false, the secret manager will throw a `com.google.api.gax.rpc.NotFoundException`.
- `spring.cloud.gcp.secretmanager.credentials.default-scopes` (Third party; type `String[]`)
- `spring.cloud.gcp.secretmanager.enabled` (Third party; type `Boolean`; default `true`): Auto-configure GCP Secret Manager support components.
- `spring.cloud.gcp.secretmanager.project-id` (Third party; type `String`): Overrides the GCP Project ID specified in the Core module.
- `spring.config.import` (Third party; type `List<String>`): Import additional config data.

## Secret Manager Config Data Resource

The functionality presented here enables you to use Secret Manager as an external config data resource which means it allows you to specify and load secrets from Google Cloud Secret Manager as properties into the application context using Spring Boot’s Config Data API.

The Secret Manager config data resource uses the following syntax to specify secrets:

```properties
# 1. Long form - specify the project ID, secret ID, and version
sm@projects/<project-id>/secrets/<secret-id>/versions/<version-id>}                           

# 2.  Long form - specify project ID, secret ID, and use latest version
sm@projects/<project-id>/secrets/<secret-id>

# 3. Short form - specify project ID, secret ID, and version
sm@<project-id>/<secret-id>/<version-id>

# 4. Short form - default project; specify secret + version
#
# The project is inferred from the Secret Manager project id setting
# in your bootstrap.properties (see Configuration) or from application-default credentials if
# this is not set.
sm@<secret-id>/<version>

# 5. Shortest form - specify secret ID, use default project and latest version.
sm@<secret-id>
```

You can use this syntax in your CAS configuration. Depending on your setup, `sm@` may also need to be listed among the Spring configuration imports.

```properties
# Example of the project-secret long-form syntax.
cas.some.property=${sm@projects/<project-id>/secrets/cas_some_property}
```
