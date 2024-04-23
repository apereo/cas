package org.apereo.cas.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.SimpleUrlValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @since 3.1
 */
@Tag("Authentication")
@SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class)
class SimpleWebApplicationServiceImplTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "simpleWebApplicationServiceImpl.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Test
    void verifySerializeACompletePrincipalToJson() throws IOException {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.CONST_TEST_URL);
        val serviceWritten = new WebApplicationServiceFactory().createService(request);
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        val serviceRead = MAPPER.readValue(JSON_FILE, SimpleWebApplicationServiceImpl.class);
        assertEquals(serviceWritten, serviceRead);
    }

    @Test
    void verifyResponse() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.CONST_TEST_URL);
        val impl = new WebApplicationServiceFactory().createService(request);

        val response = new WebApplicationServiceResponseBuilder(servicesManager, SimpleUrlValidator.getInstance())
            .build(impl, "ticketId", RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.responseType());
    }

    @Test
    void verifyCreateSimpleWebApplicationServiceImplFromServiceAttribute() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.CONST_TEST_URL);
        val impl = new WebApplicationServiceFactory().createService(request);
        assertNotNull(impl);
    }

    @Test
    void verifyResponseForJsession() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "http://www.cnn.com/;jsession=test");
        val impl = new WebApplicationServiceFactory().createService(request);

        assertEquals("http://www.cnn.com/", impl.getId());
    }

    @Test
    void verifyResponseWithNoTicket() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.CONST_TEST_URL);
        val impl = new WebApplicationServiceFactory().createService(request);

        val response = new WebApplicationServiceResponseBuilder(servicesManager, SimpleUrlValidator.getInstance())
            .build(impl, null, RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.responseType());
        assertFalse(response.url().contains("ticket="));
    }

    @Test
    void verifyResponseWithNoTicketAndNoParameterInServiceURL() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "http://foo.com/");
        val impl = new WebApplicationServiceFactory().createService(request);
        val response = new WebApplicationServiceResponseBuilder(servicesManager, SimpleUrlValidator.getInstance())
            .build(impl, null,
                RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.responseType());
        assertFalse(response.url().contains("ticket="));
        assertEquals("http://foo.com/", response.url());
    }

    @Test
    void verifyResponseWithNoTicketAndOneParameterInServiceURL() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "http://foo.com/?param=test");
        val impl = new WebApplicationServiceFactory().createService(request);
        val response = new WebApplicationServiceResponseBuilder(servicesManager, SimpleUrlValidator.getInstance())
            .build(impl, null, RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.responseType());
        assertEquals("http://foo.com/?param=test", response.url());
    }                                                                   
}
