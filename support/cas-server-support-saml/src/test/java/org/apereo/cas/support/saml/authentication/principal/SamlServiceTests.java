package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.DefaultServiceMatchingStrategy;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.config.CasSamlAutoConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.services.mgmt.DefaultServicesManager;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link SamlService}.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Import(CasSamlAutoConfiguration.class)
@Tag("SAML")
class SamlServiceTests extends AbstractOpenSamlTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "samlService.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();


    @Autowired
    @Qualifier(ServicesManagerConfigurationContext.BEAN_NAME)
    private ServicesManagerConfigurationContext servicesManagerConfigurationContext;

    @Autowired
    @Qualifier(UrlValidator.BEAN_NAME)
    private UrlValidator urlValidator;

    @Autowired
    @Qualifier("samlServiceFactory")
    private ServiceFactory<SamlService> samlServiceFactory;

    @Test
    void verifyResponse() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "service");
        val impl = samlServiceFactory.createService(request);

        val response = new SamlServiceResponseBuilder(new DefaultServicesManager(servicesManagerConfigurationContext), this.urlValidator)
            .build(impl, "ticketId", CoreAuthenticationTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.responseType());
        assertTrue(response.url().contains(SamlProtocolConstants.CONST_PARAM_ARTIFACT.concat("=")));
    }

    @Test
    void verifyResponseForJsession() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "http://www.cnn.com/;jsession=test");
        val impl = samlServiceFactory.createService(request);

        assertEquals("http://www.cnn.com/", impl.getId());
    }

    @Test
    void verifyResponseWithNoTicket() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "service");
        val impl = samlServiceFactory.createService(request);
        val response = new SamlServiceResponseBuilder(new DefaultServicesManager(servicesManagerConfigurationContext), this.urlValidator)
            .build(impl, null, CoreAuthenticationTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.responseType());
        assertFalse(response.url().contains(SamlProtocolConstants.CONST_PARAM_ARTIFACT.concat("=")));
    }

    @Test
    void verifyRequestBody() throws Throwable {
        val body = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<SOAP-ENV:Header/><SOAP-ENV:Body><samlp:Request xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" MajorVersion=\"1\" "
            + "MinorVersion=\"1\" RequestID=\"_192.168.16.51.1024506224022\" IssueInstant=\"2002-06-19T17:03:44.022Z\">"
            + "<samlp:AssertionArtifact> \n\n   artifact    \n\n   </samlp:AssertionArtifact></samlp:Request></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        val request = new MockHttpServletRequest();
        request.setRequestURI(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE);
        request.setMethod("POST");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));

        val impl = samlServiceFactory.createService(request);
        assertEquals("artifact", impl.getArtifactId());
        assertEquals("_192.168.16.51.1024506224022", impl.getRequestId());
    }

    @Test
    void verifyTargetMatchingSamlService() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "https://some.service.edu/path/to/app");

        val service = new DefaultArgumentExtractor(samlServiceFactory).extractService(request);
        val impl = new DefaultArgumentExtractor(samlServiceFactory).extractService(request);
        val manager = mock(ServicesManager.class);
        assertTrue(new DefaultServiceMatchingStrategy(manager).matches(impl, service));
    }

    @Test
    void verifyTargetMatchesNoSamlService() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "https://some.service.edu/path/to/app");
        val impl = new DefaultArgumentExtractor(samlServiceFactory).extractService(request);

        val request2 = new MockHttpServletRequest();
        request2.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "https://some.SERVICE.edu");
        val service = new DefaultArgumentExtractor(samlServiceFactory).extractService(request2);
        val manager = mock(ServicesManager.class);
        assertFalse(new DefaultServiceMatchingStrategy(manager).matches(impl, service));
    }

    @Test
    void verifySerializeASamlServiceToJson() throws IOException {
        val body = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<SOAP-ENV:Header/><SOAP-ENV:Body><samlp:Request xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" MajorVersion=\"1\" "
            + "MinorVersion=\"1\" RequestID=\"_192.168.16.51.1024506224022\" IssueInstant=\"2002-06-19T17:03:44.022Z\">"
            + "<samlp:AssertionArtifact>artifact</samlp:AssertionArtifact></samlp:Request></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        val request = new MockHttpServletRequest();
        request.setContent(body.getBytes(StandardCharsets.UTF_8));

        val serviceWritten = samlServiceFactory.createService(request);
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        val serviceRead = MAPPER.readValue(JSON_FILE, SamlService.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
