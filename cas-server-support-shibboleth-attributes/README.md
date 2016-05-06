# Shibboleth Attributes for CAS

*EXPERIMENTAL*

The module allows the deployer to use a shibboleth `attribute-resolver.xml` style file to define attributes.

For a fully working example, see [https://github.com/UniconLabs/cas-gradle-sample/tree/shibboleth-attributes]

## Integration steps

1. Modify `build.gradle` or `pom.xml` to add the dependency

        compile "org.apereo.cas:cas-server-support-shibboleth-attributes:${project.'cas.version'}"

    Note that, at least in the case of gradle, you'll need to add the Shibboleth repositories as well

        maven { url "https://build.shibboleth.net/nexus/content/repositories/releases" }

1. Alias in the DAO. The bean is called `shibbolethPersonAttributeDao`

        <alias name="shibbolethPersonAttributeDao" alias="attributeRepository" />

1. Modify either `application.properties` or the runtime environment to add `shibboleth.attributeResolver.resources`. This is a
comma seperated list of resources to use for the configuration

        -Dshibboleth.attributeResolver.resources=classpath:attribute-resolver.xml

