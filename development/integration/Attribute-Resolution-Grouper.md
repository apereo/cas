# Grouper Attribute Resolution

Source: https://apereo.github.io/cas/development/integration/Attribute-Resolution-Grouper.html

The following configuration describes how to fetch and retrieve attributes from Grouper attribute repositories.

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

This option reads all the groups from [Grouper](https://github.com/Internet2/grouper) for the given CAS principal and adopts them as CAS attributes under a `grouperGroups` multi-valued attribute.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.attribute-repository.grouper.id` (Optional; type `String`): A value can be assigned to this field to uniquely identify this resolver.
- `cas.authn.attribute-repository.grouper.order` (Optional; type `Integer`; default `0`): The order of this attribute repository in the chain of repositories. Can be used to explicitly position this source in chain and affects merging strategies.
- `cas.authn.attribute-repository.grouper.parameters` (Optional; type `Map<String,String>`): Custom parameters defined as a `Map` to pass onto the attribute repository which ultimately will be passed onto the grouper client. Key is the parameter name and value is the parameter value.
- `cas.authn.attribute-repository.grouper.state` (Optional; type `AttributeRepositoryStates`; default `active`): Whether attribute resolution based on this source is enabled. Available values are as follows: - `ACTIVE`: Active and enabled repository, and is invoked by default automatically. - `DISABLED`: Attribute repository is disabled and will not be used to resolve people and attributes. - `STANDBY`: Repository is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for and will not be registered into the resolution plan.
- `cas.authn.attribute-repository.grouper.subject-type` (Optional; type `String`; default `SUBJECT_ID`): Indicate how the username passed to the attribute repository should be set and treated by the grouper client to look up records. Accepted values are: `SUBJECT_IDENTIFIER`, `SUBJECT_ATTRIBUTE_NAME`, `SUBJECT_ID`.
- `cas.authn.attribute-repository.grouper.username-attribute` (Optional; type `String`; default `username`): The attribute name that would be used to look up and determine the user id from the query map. The value linked to this attribute would be used as the username or subject by the attribute repository to pass on to the ultimate source to locate the user record.

You will also need to ensure `grouper.client.properties` is available on the classpath (i.e. `src/main/resources`) with the following configured properties:

```properties
# grouperClient.webService.url = http://192.168.99.100:32768/grouper-ws/servicesRest
# grouperClient.webService.login = banderson
# grouperClient.webService.password = password
```

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
