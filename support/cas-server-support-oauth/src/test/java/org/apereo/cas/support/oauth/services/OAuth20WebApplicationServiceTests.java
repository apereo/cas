package org.apereo.cas.support.oauth.services;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class OAuth20WebApplicationServiceTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oAuthWebApplicationService.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeACompletePrincipalToJson() throws IOException {
        val service = new OAuthRegisteredService();
        service.setName("checkCloning");
        service.setServiceId("testId");
        service.setTheme("theme");
        service.setDescription("description");
        val factory = new WebApplicationServiceFactory();
        val serviceWritten = factory.createService(service.getServiceId());
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        val serviceRead = MAPPER.readValue(JSON_FILE, WebApplicationService.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
