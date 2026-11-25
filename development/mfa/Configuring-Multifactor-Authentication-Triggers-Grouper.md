# Grouper - Multifactor Authentication Triggers

Source: https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication-Triggers-Grouper.html

MFA can be triggered by [Grouper](https://github.com/Internet2/grouper) groups to which the authenticated principal is assigned. Groups are collected by CAS and then cross-checked against all available/configured MFA providers. The group’s comparing factor **MUST** be defined in CAS to activate this behavior and it can be based on the group’s name, display name, etc where a successful match against a provider id shall activate the chosen MFA provider.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-grouper"
}
```

You will also need to ensure `grouper.client.properties` is available on the classpath with the following configured properties:

```properties
grouperClient.webService.url = http://192.168.99.100:32768/grouper-ws/servicesRest
grouperClient.webService.login = banderson
grouperClient.webService.password = password
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.triggers.grouper.grouper-group-field` (Optional; type `String`): MFA can be triggered by Grouper groups to which the authenticated principal is assigned. Groups are collected by CAS and then cross-checked against all available/configured MFA providers. The group’s comparing factor MUST be defined in CAS to activate this behavior and it can be based on the group’s name, display name, etc where a successful match against a provider id shall activate the chosen MFA provider.

You may also consider externalizing the Grouper client configuration file by modifying `/src/main/resources/grouper.client.properties` to contain the following:

```properties
grouperClient.config.hierarchy = classpath:/grouper.client.base.properties,file:/etc/cas/config/grouper.client.properties
```

…where `/etc/cas/config/grouper.client.properties` would then contain the actual Grouper related settings.

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="edu.internet2.middleware" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
