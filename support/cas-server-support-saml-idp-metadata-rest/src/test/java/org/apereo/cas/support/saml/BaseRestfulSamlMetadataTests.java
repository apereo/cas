package org.apereo.cas.support.saml;

import org.apereo.cas.config.SamlIdPRestfulIdPMetadataConfiguration;
import org.apereo.cas.config.SamlIdPRestfulMetadataConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link BaseRestfulSamlMetadataTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    SamlIdPRestfulIdPMetadataConfiguration.class,
    SamlIdPRestfulMetadataConfiguration.class,
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.saml-idp.metadata.rest.url=http://localhost:8078",
        "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml"
    }
)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BaseRestfulSamlMetadataTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("restSamlRegisteredServiceMetadataResolver")
    protected SamlRegisteredServiceMetadataResolver resolver;
}
