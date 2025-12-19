package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link LoggingConfigurationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = {
    "management.endpoint.loggingConfig.access=UNRESTRICTED",
    "logging.config=file:${java.io.tmpdir}/log4j2.xml"
})
@Tag("ActuatorEndpoint")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoggingConfigurationEndpointTests extends AbstractCasEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .build().toObjectMapper();

    @BeforeAll
    public static void setup() throws Exception {
        val content = IOUtils.toString(new ClassPathResource("log4j2-test.xml.template").getInputStream(), StandardCharsets.UTF_8);
        try (val writer = new FileWriter(new File(FileUtils.getTempDirectory(), "log4j2.xml"), StandardCharsets.UTF_8)) {
            IOUtils.write(content, writer);
            writer.flush();
        }
    }

    @Test
    @Order(1)
    void verifyOperation() throws Throwable {
        val body = MAPPER.readValue(mockMvc.perform(get("/actuator/loggingConfig")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), Map.class);
        assertTrue(body.containsKey("loggers"));
        assertTrue(body.containsKey("activeLoggers"));
    }

    @Test
    @Order(10)
    void verifyStreamOperation() throws Throwable {
        LOGGER.warn("This is a test warning");
        val entries = MAPPER.readValue(mockMvc.perform(get("/actuator/loggingConfig/stream")
                .queryParam("level", "warn")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), List.class);
        assertFalse(entries.isEmpty());
    }
}
