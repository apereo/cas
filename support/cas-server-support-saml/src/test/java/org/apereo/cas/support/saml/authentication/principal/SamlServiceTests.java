package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.config.SamlConfiguration;
import org.apereo.cas.config.authentication.support.SamlAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.authentication.support.SamlServiceFactoryConfiguration;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.web.support.DefaultArgumentExtractor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link SamlService}.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Import({
    SamlAuthenticationEventExecutionPlanConfiguration.class,
    SamlServiceFactoryConfiguration.class,
    SamlConfiguration.class
})
public class SamlServiceTests extends AbstractOpenSamlTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "samlService.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();


    @Autowired
    @Qualifier("samlServiceFactory")
    private ServiceFactory<SamlService> samlServiceFactory;

    @Test
    public void verifyResponse() {
        val request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "service");
        val impl = samlServiceFactory.createService(request);

        val response = new SamlServiceResponseBuilder(
            new DefaultServicesManager(mock(ServiceRegistry.class), mock(ApplicationEventPublisher.class), new HashSet<>()))
            .build(impl, "ticketId", CoreAuthenticationTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
        assertTrue(response.getUrl().contains(SamlProtocolConstants.CONST_PARAM_ARTIFACT.concat("=")));
    }

    @Test
    public void verifyResponseForJsession() {
        val request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "http://www.cnn.com/;jsession=test");
        val impl = samlServiceFactory.createService(request);

        assertEquals("http://www.cnn.com/", impl.getId());
    }

    @Test
    public void verifyResponseWithNoTicket() {
        val request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "service");
        val impl = samlServiceFactory.createService(request);
        val response = new SamlServiceResponseBuilder(
            new DefaultServicesManager(mock(ServiceRegistry.class), mock(ApplicationEventPublisher.class), new HashSet<>()))
            .build(impl, null, CoreAuthenticationTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.getResponseType());
        assertFalse(response.getUrl().contains(SamlProtocolConstants.CONST_PARAM_ARTIFACT.concat("=")));
    }

    @Test
    public void verifyRequestBody() {
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
    public void verifyTargetMatchingSamlService() {
        val request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "https://some.service.edu/path/to/app");

        val service = new DefaultArgumentExtractor(samlServiceFactory).extractService(request);
        val impl = new DefaultArgumentExtractor(samlServiceFactory).extractService(request);
        assertTrue(impl.matches(service));
    }

    @Test
    public void verifyTargetMatchesNoSamlService() {
        val request = new MockHttpServletRequest();
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "https://some.service.edu/path/to/app");
        val impl = new DefaultArgumentExtractor(samlServiceFactory).extractService(request);

        val request2 = new MockHttpServletRequest();
        request2.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "https://some.SERVICE.edu");
        val service = new DefaultArgumentExtractor(samlServiceFactory).extractService(request2);
        assertFalse(impl.matches(service));
    }

    @Test
    public void verifySerializeASamlServiceToJson() throws IOException {
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
