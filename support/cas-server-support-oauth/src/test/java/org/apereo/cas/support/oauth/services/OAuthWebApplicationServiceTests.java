package org.apereo.cas.support.oauth.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.AbstractRegisteredService;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class OAuthWebApplicationServiceTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oAuthWebApplicationService.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeACompletePrincipalToJson() throws IOException {
        final AbstractRegisteredService service = new OAuthRegisteredService();
        service.setName("checkCloning");
        service.setServiceId("testId");
        service.setTheme("theme");
        service.setDescription("description");
        final WebApplicationServiceFactory factory = new WebApplicationServiceFactory();
        final WebApplicationService serviceWritten = factory.createService(service.getServiceId());
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        final WebApplicationService serviceRead = MAPPER.readValue(JSON_FILE, WebApplicationService.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
