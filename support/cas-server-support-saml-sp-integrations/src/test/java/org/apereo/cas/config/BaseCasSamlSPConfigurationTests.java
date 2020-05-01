package org.apereo.cas.config;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

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
    BaseSamlIdPConfigurationTests.SharedTestConfiguration.class
})
public abstract class BaseCasSamlSPConfigurationTests {
    protected static String SERVICE_PROVIDER;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @DynamicPropertySource
    private static void configurePropertySource(final DynamicPropertyRegistry registry) {
        registry.add("cas.samlSp." + SERVICE_PROVIDER + ".metadata", () -> "classpath:/metadata/sp-metadata.xml");
        registry.add("cas.samlSp." + SERVICE_PROVIDER + ".nameIdAttribute", () -> "cn");
        registry.add("cas.samlSp." + SERVICE_PROVIDER + ".nameIdFormat", () -> "transient");
    }
    
    @AfterEach
    public void afterEach() {
        servicesManager.deleteAll();
    }

    @Test
    public void verifyOperation() {
        assertNotNull(servicesManager.findServiceBy("https://example.org/shibboleth", SamlRegisteredService.class));
    }
}
