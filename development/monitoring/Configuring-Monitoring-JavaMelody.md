# JavaMelody Monitoring

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Monitoring-JavaMelody.html

Use [JavaMelody](https://github.com/javamelody/javamelody) to monitor CAS in QA and production environments.

> **Usage Warning!**
>
> This functionality is not yet compatible with the CAS ecosystem of libraries, and/or Spring Boot. As a result, support for this feature is dropped. Please consider using alternatives and keep an eye on future releases of CAS to see if support for this feature is re-introduced.

Support is added by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-javamelody"
}
```

JavaMelody monitoring is by default exposed at `${context-path}/monitoring` where `${context-path}` is typically set to `/cas`.

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `javamelody.enabled` (Third party; type `Boolean`; default `true`): If JavaMelody should be enabled within the application.
- `javamelody.excluded-datasources` (Third party; type `String`): Comma-separated list of data source names which should be excluded from monitoring.
- `javamelody.init-parameters` (Third party; type `Map<String,String>`): Map of initialization parameters to be passed to the JavaMelody monitoring filter. The available parameters are listed here: https://github.com/javamelody/javamelody/wiki/UserGuide#6-optional-parameters
- `javamelody.management-endpoint-monitoring-enabled` (Third party; type `Boolean`; default `false`): If /monitoring should be enabled for reports in the management http port instead of on the application http port.
- `javamelody.spring-monitoring-enabled` (Third party; type `Boolean`; default `true`): If monitoring based on @RestController, @Controller, @Service, @Async, @Scheduled, @MonitoredWithSpring, @FeignClient or on RestTemplate, MongoDatabaseFactory or ElasticsearchOperations should be enabled.
