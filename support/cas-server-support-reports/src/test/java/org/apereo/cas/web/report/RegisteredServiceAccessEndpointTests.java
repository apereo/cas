package org.apereo.cas.web.report;

import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
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
@TestPropertySource(properties = {
    "cas.authn.attribute-repository.stub.attributes.uid=cas",
    "cas.authn.attribute-repository.stub.attributes.givenName=apereo-cas",
    "cas.authn.attribute-repository.stub.attributes.phone=123456789",
    "management.endpoint.serviceAccess.access=UNRESTRICTED",
    "cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS"
})
class RegisteredServiceAccessEndpointTests extends AbstractCasEndpointTests {
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
    void verifyHead() throws Throwable {
        mockMvc.perform(head("/actuator/serviceAccess")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
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
