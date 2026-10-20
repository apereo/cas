package org.apereo.cas.support.saml;

import module java.base;
import org.apereo.cas.config.CasSamlIdPRedisIdPMetadataAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link BaseRedisSamlMetadataTests}.
 * Every test context shares the same Redis server, where the identity provider's global metadata document is
 * generated once by whichever context starts first and then used by all of them; the crypto keys are fixed so
 * that every context can decrypt the keys of that document, which random keys per context would not allow.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = {
    CasSamlIdPRedisIdPMetadataAutoConfiguration.class,
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.saml-idp.metadata.redis.crypto.encryption.key=BXRiSBWJcRksTizjdaCoLw",
    "cas.authn.saml-idp.metadata.redis.crypto.signing.key=cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseRedisSamlMetadataTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("redisSamlRegisteredServiceMetadataResolver")
    protected SamlRegisteredServiceMetadataResolver resolver;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(SamlIdPMetadataGenerator.BEAN_NAME)
    protected SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @Autowired
    @Qualifier(SamlIdPMetadataLocator.BEAN_NAME)
    protected SamlIdPMetadataLocator samlIdPMetadataLocator;
}
