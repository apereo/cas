package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.config.SamlGoogleAppsConfiguration;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link GoogleAccountsServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 * @deprecated Since 6.2
 */
@SpringBootTest(classes = {
    SamlGoogleAppsConfiguration.class,
    AbstractOpenSamlTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true",

    "cas.server.name=http://localhost:8080",
    "cas.server.prefix=${server.name}/cas",

    "cas.saml-core.issuer=localhost",
    "cas.saml-core.skew-allowance=200",
    "cas.saml-core.ticketid-saml2=false",
    
    "cas.google-apps.key-algorithm=DSA",
    "cas.google-apps.public-key-location=classpath:DSAPublicKey01.key",
    "cas.google-apps.private-key-location=classpath:DSAPrivateKey01.key"
})
@Tag("SAML")
@Deprecated(since = "6.2.0")
public class GoogleAccountsServiceFactoryTests extends AbstractOpenSamlTests {
    @Autowired
    @Qualifier("googleAccountsServiceFactory")
    private ServiceFactory factory;

    @Autowired
    @Qualifier("googleAccountsServiceResponseBuilder")
    private ResponseBuilder<GoogleAccountsService> googleAccountsServiceResponseBuilder;

    @Autowired
    private ApplicationContextProvider applicationContextProvider;

    private static String encodeMessage(final String xmlString) {
        return CompressionUtils.deflate(xmlString);
    }

    @BeforeEach
    public void init() {
        this.applicationContextProvider.setApplicationContext(this.applicationContext);
    }

    @Test
    public void verifyNoService() {
        assertNull(factory.createService(new MockHttpServletRequest()));
    }

    @Test
    public void verifyAuthnRequest() {
        val request = new MockHttpServletRequest();
        val samlRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
            + "ID=\"5545454455\" Version=\"2.0\" IssueInstant=\"Value\" "
            + "ProtocolBinding=\"urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect\" "
            + "ProviderName=\"https://localhost:8443/myRutgers\" AssertionConsumerServiceURL=\"https://localhost:8443/myRutgers\"/>";
        request.setParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST, encodeMessage(samlRequest));

        val service = (GoogleAccountsService) this.factory.createService(request);
        service.setPrincipal(CoreAuthenticationTestUtils.getPrincipal().getId());
        assertNotNull(service);
        val response = googleAccountsServiceResponseBuilder.build(service, "SAMPLE_TICKET",
            CoreAuthenticationTestUtils.getAuthentication());
        assertNotNull(response);
    }
}
