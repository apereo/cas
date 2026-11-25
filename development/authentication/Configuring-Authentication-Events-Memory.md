# Memory Authentication Events

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Events-Memory.html

Stores authentication events into memory for a very limited time period.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-events-memory"
}
```
