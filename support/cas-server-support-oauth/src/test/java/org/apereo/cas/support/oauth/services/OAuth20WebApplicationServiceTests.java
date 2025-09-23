package org.apereo.cas.support.oauth.services;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Tag("OAuth")
class OAuth20WebApplicationServiceTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeACompletePrincipalToJson() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val service = new OAuthRegisteredService();
        service.setName("checkCloning");
        service.setServiceId("testId");
        service.setTheme("theme");
        service.setDescription("description");
        val factory = RegisteredServiceTestUtils.getWebApplicationServiceFactory();
        val serviceWritten = factory.createService(service.getServiceId());
        MAPPER.writeValue(jsonFile, serviceWritten);
        val serviceRead = MAPPER.readValue(jsonFile, WebApplicationService.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
