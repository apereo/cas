package org.apereo.cas.support.saml;

import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.SamlIdPJpaIdPMetadataConfiguration;
import org.apereo.cas.config.SamlIdPJpaRegisteredServiceMetadataConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link BaseJpaSamlMetadataTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    SamlIdPJpaRegisteredServiceMetadataConfiguration.class,
    SamlIdPJpaIdPMetadataConfiguration.class,
    CasHibernateJpaConfiguration.class,
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=true",
    "cas.authn.saml-idp.metadata.location=${#systemProperties['java.io.tmpdir']}/saml"
})
public abstract class BaseJpaSamlMetadataTests {
    @Autowired
    @Qualifier("jpaSamlRegisteredServiceMetadataResolver")
    protected SamlRegisteredServiceMetadataResolver resolver;

    @Autowired
    @Qualifier("samlIdPMetadataGenerator")
    protected SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @Autowired
    @Qualifier("samlIdPMetadataLocator")
    protected SamlIdPMetadataLocator samlIdPMetadataLocator;
}
