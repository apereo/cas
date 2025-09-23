package org.apereo.cas.pm.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.pm.web.flow.actions.BasePasswordManagementActionTests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link PasswordManagementEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@TestPropertySource(properties = {
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
    
    @Test
    void verifyOperation() throws Throwable {
        val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
        servicesManager.save(registeredService);
        
        mockMvc.perform(post("/actuator/passwordManagement/reset/casuser")
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

        mockMvc.perform(post("/actuator/passwordManagement/reset/none")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .queryParam(CasProtocolConstants.PARAMETER_SERVICE, service.getId())
        ).andExpect(status().isUnprocessableEntity());
    }
}
