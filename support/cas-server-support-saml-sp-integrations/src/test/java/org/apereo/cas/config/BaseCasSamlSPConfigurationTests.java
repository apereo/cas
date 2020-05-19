package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseCasSamlSPConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    BaseSamlIdPConfigurationTests.SharedTestConfiguration.class,
    CasSamlServiceProvidersConfiguration.class
}, properties = {
    "cas.authn.saml-idp.entityId=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.location=${#systemProperties['java.io.tmpdir']}/sp-idp-metadata"
})
@Slf4j
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class BaseCasSamlSPConfigurationTests {

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    @AfterEach
    public void afterEach() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyOperation() {
        LOGGER.debug("Looking for service id [{}]", getServiceProviderId());
        assertNotNull(servicesManager.findServiceBy(getServiceProviderId(), SamlRegisteredService.class));
    }

    protected String getServiceProviderId() {
        return "https://example.org/shibboleth";
    }
}
