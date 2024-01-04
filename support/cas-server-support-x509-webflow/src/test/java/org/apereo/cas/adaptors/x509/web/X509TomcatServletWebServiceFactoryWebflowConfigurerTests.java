package org.apereo.cas.adaptors.x509.web;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.X509AuthenticationAutoConfiguration;
import org.apereo.cas.config.X509AuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.X509CertificateExtractorAutoConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509TomcatServletWebServiceFactoryWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Import({
    WebMvcAutoConfiguration.class,
    ServletWebServerFactoryAutoConfiguration.class,
    X509AuthenticationAutoConfiguration.class,
    X509CertificateExtractorAutoConfiguration.class,
    X509AuthenticationWebflowAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class
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
    void verifyOperation() throws Throwable {
        assertNotNull(webflowConfigurer);
    }

}
