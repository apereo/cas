package org.apereo.cas.adaptors.x509.web;

import org.apereo.cas.adaptors.x509.config.X509AuthenticationConfiguration;
import org.apereo.cas.adaptors.x509.config.X509CertificateExtractorConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.X509AuthenticationWebflowConfiguration;

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
    X509AuthenticationConfiguration.class,
    X509CertificateExtractorConfiguration.class,
    X509AuthenticationWebflowConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@Tag("WebflowConfig")
@TestPropertySource(properties = {
    "server.ssl.key-store=file:/tmp/keystore",
    "server.ssl.key-store-password=changeit",

    "server.ssl.trust-store=file:/tmp/thekeystore",
    "server.ssl.trust-store-password=changeit",

    "cas.authn.x509.webflow.port=9876"
})
public class X509TomcatServletWebServiceFactoryWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("x509TomcatServletWebServiceFactoryWebflowConfigurer")
    private CasWebflowConfigurer webflowConfigurer;

    @Test
    public void verifyOperation() {
        assertNotNull(webflowConfigurer);
    }

}
