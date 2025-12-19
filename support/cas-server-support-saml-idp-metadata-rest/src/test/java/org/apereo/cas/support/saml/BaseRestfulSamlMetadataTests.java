package org.apereo.cas.support.saml;

import module java.base;
import org.apereo.cas.config.CasSamlIdPRestfulMetadataAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.extension.ExtendWith;
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
    CasSamlIdPRestfulMetadataAutoConfiguration.class,
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.saml-idp.metadata.rest.url=http://localhost:8078",
        "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml33"
    }
)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseRestfulSamlMetadataTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("restSamlRegisteredServiceMetadataResolver")
    protected SamlRegisteredServiceMetadataResolver resolver;
}
