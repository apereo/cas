package org.apereo.cas.pm.web;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.web.flow.actions.BasePasswordManagementActionTests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link PasswordManagementEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.history.core.enabled=true",
    "cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS",
    "management.endpoints.web.exposure.include=*",
    "management.endpoint.passwordManagement.access=UNRESTRICTED"
})
@ExtendWith(CasTestExtension.class)
@Tag("Mail")
@AutoConfigureMockMvc
@SpringBootTestAutoConfigurations
@EnabledIfListeningOnPort(port = 25000)
class PasswordManagementEndpointTests extends BasePasswordManagementActionTests {

    @Autowired
    @Qualifier("mockMvc")
    protected MockMvc mockMvc;

    @Autowired
    @Qualifier(PasswordHistoryService.BEAN_NAME)
    protected PasswordHistoryService passwordHistoryService;

    @Test
    void verifyOperation() throws Throwable {
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
        servicesManager.save(registeredService);
        
        mockMvc.perform(post("/actuator/passwordManagement/reset/requests/casuser")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .queryParam(CasProtocolConstants.PARAMETER_SERVICE, service.getId())
        ).andExpect(status().isOk());
    }

    @Test
    void verifyNoEmailOrPhone() throws Throwable {
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
        servicesManager.save(registeredService);

        mockMvc.perform(post("/actuator/passwordManagement/reset/requests/none")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .queryParam(CasProtocolConstants.PARAMETER_SERVICE, service.getId())
        ).andExpect(status().isUnprocessableContent());
    }

    @Test
    void verifyPasswordHistory() throws Throwable {
        val username = UUID.randomUUID().toString();
        val changeRequest = new PasswordChangeRequest(username, "current".toCharArray(),
            "newPassword1".toCharArray(), "newPassword1".toCharArray());
        passwordHistoryService.store(changeRequest);

        mockMvc.perform(get("/actuator/passwordManagement/history/" + username)
            .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(Matchers.greaterThan(0)));
    }

    @Test
    void verifyRemovePasswordHistory() throws Throwable {
        val username = UUID.randomUUID().toString();
        val changeRequest = new PasswordChangeRequest(username, "current".toCharArray(),
            "newPassword1".toCharArray(), "newPassword1".toCharArray());
        passwordHistoryService.store(changeRequest);

        mockMvc.perform(delete("/actuator/passwordManagement/history/" + username)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        mockMvc.perform(get("/actuator/passwordManagement/history/" + username)
            .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(0));
    }
}
