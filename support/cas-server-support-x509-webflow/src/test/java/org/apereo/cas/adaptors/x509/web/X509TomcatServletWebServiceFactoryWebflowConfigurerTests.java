package org.apereo.cas.adaptors.x509.web;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasX509AuthenticationAutoConfiguration;
import org.apereo.cas.config.CasX509AuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasX509CertificateExtractorAutoConfiguration;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509TomcatServletWebServiceFactoryWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTestAutoConfigurations
@ImportAutoConfiguration({
    CasX509AuthenticationAutoConfiguration.class,
    CasX509CertificateExtractorAutoConfiguration.class,
    CasX509AuthenticationWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class
})
@Tag("WebflowConfig")
@TestPropertySource(properties = {
    "server.ssl.key-store=file:/tmp/keystore-${#randomNumber6}.jks",
    "server.ssl.key-store-password=changeit",

    "server.ssl.trust-store=file:/tmp/thekeystore",
    "server.ssl.trust-store-password=changeit",

    "cas.authn.x509.webflow.port=9876"
})
class X509TomcatServletWebServiceFactoryWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("x509TomcatServletWebServiceFactoryWebflowConfigurer")
    private CasWebflowConfigurer webflowConfigurer;

    @Test
    void verifyOperation() {
        assertNotNull(webflowConfigurer);
    }

}
