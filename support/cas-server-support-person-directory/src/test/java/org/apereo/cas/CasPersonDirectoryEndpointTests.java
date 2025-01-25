package org.apereo.cas;

import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.persondir.MockPersonAttributeDao;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link CasPersonDirectoryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@TestPropertySource(properties = "management.endpoint.personDirectory.access=UNRESTRICTED")
@Import(CasPersonDirectoryEndpointTests.CasPersonDirectoryTestConfiguration.class)
@Tag("ActuatorEndpoint")
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
class CasPersonDirectoryEndpointTests extends AbstractCasEndpointTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final MockPersonAttributeDao ATTRIBUTE_DAO = new MockPersonAttributeDao(
        new SimplePersonAttributes("casuser", Map.of("phone", List.of("123456789"))));

    @Test
    void verifyRepositories() throws Throwable {
        mockMvc.perform(get("/actuator/personDirectory/repositories")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void verifyGetUser() throws Throwable {
        var person = MAPPER.readValue(mockMvc.perform(get("/actuator/personDirectory/cache/casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), SimplePersonAttributes.class);
        assertNotNull(person);
        assertEquals("123456789", person.getAttributeValue("phone"));

        mockMvc.perform(delete("/actuator/personDirectory/cache/casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        ATTRIBUTE_DAO.setPerson(new SimplePersonAttributes("casuser", Map.of("phone", List.of("99887766"))));
        
        person = MAPPER.readValue(mockMvc.perform(get("/actuator/personDirectory/cache/casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(), SimplePersonAttributes.class);
        assertNotNull(person);
        assertEquals("99887766", person.getAttributeValue("phone"));
    }

    @TestConfiguration(value = "CasPersonDirectoryTestConfiguration", proxyBeanMethods = false)
    static class CasPersonDirectoryTestConfiguration {
        @Bean
        public PersonDirectoryAttributeRepositoryPlanConfigurer testAttributeRepositoryPlanConfigurer() {
            return plan -> plan.registerAttributeRepositories(ATTRIBUTE_DAO);
        }
    }

}
