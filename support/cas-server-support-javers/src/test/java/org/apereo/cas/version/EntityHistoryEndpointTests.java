package org.apereo.cas.version;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link EntityHistoryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("MongoDb")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 27017)
@AutoConfigureMockMvc
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = BaseEntityHistoryTests.SharedTestConfiguration.class,
    properties = {
        "management.endpoints.access.default=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.entityHistory.access=UNRESTRICTED",
        "cas.javers.mongo.client-uri=mongodb://root:secret@localhost:27017/cas?authSource=admin"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class EntityHistoryEndpointTests extends BaseEntityHistoryTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;
    
    @Test
    void verifyForbiddenOperation() throws Throwable {
        mockMvc.perform(get("/actuator/entityHistory/registeredServices/112233")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test
    void verifyHistory() throws Throwable {
        val service1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val savedService = servicesManager.save(service1);
        mockMvc.perform(get("/actuator/entityHistory/registeredServices/" + savedService.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void verifyChangelog() throws Throwable {
        val service1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val savedService = servicesManager.save(service1);
        mockMvc.perform(get("/actuator/entityHistory/registeredServices/" + savedService.getId() + "/changelog"))
            .andExpect(status().isOk());
    }
}
