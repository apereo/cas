# Spring Cloud Configuration Server - Spring Cloud Kubernetes

Source: https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-Kubernetes.html

[Spring Cloud Kubernetes](https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/) provides implementations of well known Spring Cloud interfaces allowing developers to build and run Spring Cloud applications on Kubernetes, and take advantage of configuration management features in Kubernetes such as `ConfigMap`s and `Secret`s.

Support is provided via the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-configuration-cloud-kubernetes"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `spring.cloud.kubernetes.config.enabled` (Third party; type `Boolean`; default `true`): Enable the ConfigMap property source locator.
- `spring.cloud.kubernetes.config.fail-fast` (Third party; type `Boolean`; default `false`)
- `spring.cloud.kubernetes.config.include-profile-specific-sources` (Third party; type `Boolean`; default `true`)
- `spring.cloud.kubernetes.config.labels` (Third party; type `Map<String,String>`)
- `spring.cloud.kubernetes.config.name` (Third party; type `String`)
- `spring.cloud.kubernetes.config.namespace` (Third party; type `String`)
- `spring.cloud.kubernetes.config.read-type` (Third party; type `ReadType`; default `BATCH`)
- `spring.cloud.kubernetes.config.retry` (Third party; type `RetryProperties`)
- `spring.cloud.kubernetes.config.sources` (Third party; type `List<SourceConfigProperties.Source>`)
- `spring.cloud.kubernetes.config.use-name-as-prefix` (Third party; type `Boolean`; default `false`)
- `spring.cloud.kubernetes.secrets.enabled` (Third party; type `Boolean`; default `true`): Enable the Secrets property source locator.
- `spring.cloud.kubernetes.secrets.fail-fast` (Third party; type `Boolean`; default `false`)
- `spring.cloud.kubernetes.secrets.include-profile-specific-sources` (Third party; type `Boolean`; default `true`)
- `spring.cloud.kubernetes.secrets.labels` (Third party; type `Map<String,String>`)
- `spring.cloud.kubernetes.secrets.name` (Third party; type `String`)
- `spring.cloud.kubernetes.secrets.namespace` (Third party; type `String`)
- `spring.cloud.kubernetes.secrets.read-type` (Third party; type `ReadType`; default `BATCH`)
- `spring.cloud.kubernetes.secrets.retry` (Third party; type `RetryProperties`)
- `spring.cloud.kubernetes.secrets.sources` (Third party; type `List<SourceConfigProperties.Source>`)
- `spring.cloud.kubernetes.secrets.use-name-as-prefix` (Third party; type `Boolean`; default `false`)
