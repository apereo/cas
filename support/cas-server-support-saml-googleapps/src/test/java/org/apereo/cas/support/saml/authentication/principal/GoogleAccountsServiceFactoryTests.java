package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.config.SamlGoogleAppsConfiguration;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * Test cases for {@link GoogleAccountsServiceFactory}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Import(SamlGoogleAppsConfiguration.class)
@TestPropertySource(locations = "classpath:/gapps.properties")
public class GoogleAccountsServiceFactoryTests extends AbstractOpenSamlTests {
    @Autowired
    @Qualifier("googleAccountsServiceFactory")
    private ServiceFactory factory;

    @Autowired
    @Qualifier("googleAccountsServiceResponseBuilder")
    private ResponseBuilder<GoogleAccountsService> googleAccountsServiceResponseBuilder;

    @Autowired
    private ApplicationContextProvider applicationContextProvider;

    @Before
    public void init() {
        this.applicationContextProvider.setApplicationContext(this.applicationContext);
    }

    @Test
    public void verifyNoService() {
        assertNull(factory.createService(new MockHttpServletRequest()));
    }

    @Test
    public void verifyAuthnRequest() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String samlRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
                + "ID=\"5545454455\" Version=\"2.0\" IssueInstant=\"Value\" "
                + "ProtocolBinding=\"urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect\" "
                + "ProviderName=\"https://localhost:8443/myRutgers\" AssertionConsumerServiceURL=\"https://localhost:8443/myRutgers\"/>";
        request.setParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST, encodeMessage(samlRequest));

        final GoogleAccountsService service = (GoogleAccountsService) this.factory.createService(request);
        service.setPrincipal(CoreAuthenticationTestUtils.getPrincipal().getId());
        assertNotNull(service);
        final Response response = googleAccountsServiceResponseBuilder.build(service, "SAMPLE_TICKET",
                CoreAuthenticationTestUtils.getAuthentication());
        assertNotNull(response);
    }

    private static String encodeMessage(final String xmlString) {
        return CompressionUtils.deflate(xmlString);
    }
}
