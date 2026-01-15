package org.apereo.cas.support.saml;

import module java.base;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.config.CasSamlIdPJpaAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.extension.ExtendWith;
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
    CasSamlIdPJpaAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class,
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = false)
@TestPropertySource(properties = {
    "cas.jdbc.show-sql=false",
    "cas.authn.saml-idp.metadata.jpa.ddl-auto=create-drop",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml11"
})
@ExtendWith(CasTestExtension.class)
public abstract class BaseJpaSamlMetadataTests {
    @Autowired
    @Qualifier("jpaSamlRegisteredServiceMetadataResolver")
    protected SamlRegisteredServiceMetadataResolver resolver;

    @Autowired
    @Qualifier(SamlIdPMetadataGenerator.BEAN_NAME)
    protected SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @Autowired
    @Qualifier(SamlIdPMetadataLocator.BEAN_NAME)
    protected SamlIdPMetadataLocator samlIdPMetadataLocator;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    protected OpenSamlConfigBean openSamlConfigBean;
}
