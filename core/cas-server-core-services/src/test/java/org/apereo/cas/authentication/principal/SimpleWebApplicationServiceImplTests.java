package org.apereo.cas.authentication.principal;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.SimpleUrlValidator;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @author Arnaud Lesueur
 * @since 3.1
 */
@Tag("Authentication")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class)
class SimpleWebApplicationServiceImplTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "simpleWebApplicationServiceImpl.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;
    
    @Test
    void verifySerializeACompletePrincipalToJson() throws IOException {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.CONST_TEST_URL);
        val serviceWritten = webApplicationServiceFactory.createService(request);
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        val serviceRead = MAPPER.readValue(JSON_FILE, SimpleWebApplicationServiceImpl.class);
        assertEquals(serviceWritten, serviceRead);
    }

    @Test
    void verifyResponse() {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.CONST_TEST_URL);
        val service = webApplicationServiceFactory.createService(request);
        assertFalse(service.getAttributes().isEmpty());
        assertFalse(service.getAttributeAs("service", List.class).isEmpty());
        assertFalse(service.getFirstAttribute("service", String.class).isEmpty());
        assertThrows(ClassCastException.class, () -> service.getAttributeAs("service", Double.class));
        val response = new WebApplicationServiceResponseBuilder(servicesManager, SimpleUrlValidator.getInstance())
            .build(service, "ticketId", RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.responseType());
    }

    @Test
    void verifyCreateSimpleWebApplicationServiceImplFromServiceAttribute() {
        val request = new MockHttpServletRequest();
        request.setAttribute(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.CONST_TEST_URL);
        val impl = webApplicationServiceFactory.createService(request);
        assertNotNull(impl);
    }

    @Test
    void verifyResponseForJsession() {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "http://www.cnn.com/;jsession=test");
        val impl = webApplicationServiceFactory.createService(request);

        assertEquals("http://www.cnn.com/", impl.getId());
    }

    @Test
    void verifyResponseWithNoTicket() {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.CONST_TEST_URL);
        val impl = webApplicationServiceFactory.createService(request);

        val response = new WebApplicationServiceResponseBuilder(servicesManager, SimpleUrlValidator.getInstance())
            .build(impl, null, RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.responseType());
        assertFalse(response.url().contains("ticket="));
    }

    @Test
    void verifyResponseWithNoTicketAndNoParameterInServiceURL() {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "http://foo.com/");
        val impl = webApplicationServiceFactory.createService(request);
        val response = new WebApplicationServiceResponseBuilder(servicesManager, SimpleUrlValidator.getInstance())
            .build(impl, null,
                RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.responseType());
        assertFalse(response.url().contains("ticket="));
        assertEquals("http://foo.com/", response.url());
    }

    @Test
    void verifyResponseWithNoTicketAndOneParameterInServiceURL() {
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "http://foo.com/?param=test");
        val impl = webApplicationServiceFactory.createService(request);
        val response = new WebApplicationServiceResponseBuilder(servicesManager, SimpleUrlValidator.getInstance())
            .build(impl, null, RegisteredServiceTestUtils.getAuthentication());
        assertNotNull(response);
        assertEquals(Response.ResponseType.REDIRECT, response.responseType());
        assertEquals("http://foo.com/?param=test", response.url());
    }                                                                   
}
