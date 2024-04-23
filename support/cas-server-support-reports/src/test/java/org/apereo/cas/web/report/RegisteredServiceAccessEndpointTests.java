package org.apereo.cas.web.report;

import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import java.util.Set;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link RegisteredServiceAccessEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("ActuatorEndpoint")
@AutoConfigureMockMvc
@SpringBootTest(classes = AbstractCasEndpointTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.attribute-repository.stub.attributes.uid=cas",
        "cas.authn.attribute-repository.stub.attributes.givenName=apereo-cas",
        "cas.authn.attribute-repository.stub.attributes.phone=123456789",
        "server.port=8181",

        "cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS",
        "management.endpoint.serviceAccess.enabled=true",
        "management.endpoints.web.exposure.include=*"
    },
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RegisteredServiceAccessEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Test
    void verifyForbiddenOperation() throws Throwable {
        mockMvc.perform(post("/actuator/serviceAccess")
            .param("service", "https://unknown.example.edu")
            .param("username", "casuser")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test
    void verifyAccessAllowedOperation() throws Exception {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://canvas.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setRequiredAttributes(Map.of("phone", Set.of("123456789"))));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
            .param("service", "https://canvas.example.edu")
            .param("username", "casuser")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void verifyAccessWithPassword() throws Exception {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://psw.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setRequiredAttributes(Map.of("givenName", Set.of("apereo.+"))));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
            .param("service", "https://psw.example.edu")
            .param("username", "casuser")
            .param("password", "casuser")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void verifyAccessWithInvalidPassword() throws Exception {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://valid.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setRequiredAttributes(Map.of("givenName", Set.of("apereo.+"))));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
            .param("service", "https://valid.example.edu")
            .param("username", "casuser")
            .param("password", "bad-password")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }
}
