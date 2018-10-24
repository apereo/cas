package org.apereo.cas.support.saml.services;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link BaseSamlIdPServicesTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CoreSamlConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@TestPropertySource(properties = {"spring.mail.host=localhost", "spring.mail.port=25000", "spring.mail.testConnection=true"})
public abstract class BaseSamlIdPServicesTests {
    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    protected OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    protected CasConfigurationProperties casProperties;
}
