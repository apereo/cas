package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.util.ApplicationContextProvider;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test cases for {@link GoogleAccountsServiceFactory}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GoogleAccountsServiceFactoryTests extends AbstractOpenSamlTests {
    @Autowired
    private GoogleAccountsServiceFactory factory;

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

        final GoogleAccountsService service = this.factory.createService(request);
        service.setPrincipal(TestUtils.getPrincipal());
        assertNotNull(service);
        final Response response = service.getResponse("SAMPLE_TICKET");
        assertNotNull(response);
    }

    private static String encodeMessage(final String xmlString) throws IOException {
        return CompressionUtils.deflate(xmlString);
    }
}
