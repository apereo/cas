# Database Authentication

Source: https://apereo.github.io/cas/development/authentication/Database-Authentication.html

Database authentication is enabled by including the following dependencies in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-jdbc"
}
```

To learn how to configure database drivers, [please see this guide](https://apereo.github.io/cas/development/installation/JDBC-Drivers.html).

The parameters that may be passed are as follows:

| Option | Resource |
| --- | --- |
| Bind | See [this guide](https://apereo.github.io/cas/development/authentication/Database-Authentication-Bind.html). |
| Search | See [this guide](https://apereo.github.io/cas/development/authentication/Database-Authentication-Search.html). |
| Query | See [this guide](https://apereo.github.io/cas/development/authentication/Database-Authentication-Query.html). |
| Encode | See [this guide](https://apereo.github.io/cas/development/authentication/Database-Authentication-Encode.html). |
| Stored Procedures | See [this guide](https://apereo.github.io/cas/development/authentication/Database-Authentication-StoredProcedure.html). |
