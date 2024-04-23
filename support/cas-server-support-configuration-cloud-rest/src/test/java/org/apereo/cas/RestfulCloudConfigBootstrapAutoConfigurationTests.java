package org.apereo.cas;

import org.apereo.cas.config.RestfulCloudConfigBootstrapAutoConfiguration;
import org.apereo.cas.config.RestfulPropertySourceLocator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulCloudConfigBootstrapAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    RestfulCloudConfigBootstrapAutoConfiguration.class
})
@Tag("RestfulApi")
class RestfulCloudConfigBootstrapAutoConfigurationTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    private static MockWebServer SERVER;

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void setup() throws Exception {
        val response = MAPPER.writeValueAsString(Map.of("cas.authn.accept.users", STATIC_AUTHN_USERS));
        System.setProperty(RestfulPropertySourceLocator.CAS_CONFIGURATION_PREFIX + '.' + "url", "http://localhost:9345");
        SERVER = new MockWebServer(9345, new ByteArrayResource(response.getBytes(StandardCharsets.UTF_8), "Output"),
            HttpStatus.OK);
        SERVER.start();
    }

    @AfterAll
    public static void tearDown() {
        SERVER.close();
    }

    @Test
    void verifyOperation() throws Throwable {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
    }
}
