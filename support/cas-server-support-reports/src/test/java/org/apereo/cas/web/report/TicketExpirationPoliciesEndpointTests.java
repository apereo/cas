package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.services.DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProxyTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceServiceTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link TicketExpirationPoliciesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = "management.endpoint.ticketExpirationPolicies.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
class TicketExpirationPoliciesEndpointTests extends AbstractCasEndpointTests {
    @Test
    void verifyOperation() throws Exception {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val tgtPolicy = new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy().setMaxTimeToLiveInSeconds(10);
        service.setTicketGrantingTicketExpirationPolicy(tgtPolicy);
        service.setServiceTicketExpirationPolicy(new DefaultRegisteredServiceServiceTicketExpirationPolicy(10, "PT10S"));
        service.setProxyTicketExpirationPolicy(new DefaultRegisteredServiceProxyTicketExpirationPolicy(10, "PT10S"));
        service.setProxyGrantingTicketExpirationPolicy(new DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy(10));
        servicesManager.save(service);

        mockMvc.perform(get("/actuator/ticketExpirationPolicies")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isNotEmpty());

        mockMvc.perform(get("/actuator/ticketExpirationPolicies")
                .param("serviceId", service.getServiceId())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isNotEmpty());

        mockMvc.perform(get("/actuator/ticketExpirationPolicies")
                .param("serviceId", String.valueOf(service.getId()))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isNotEmpty());
    }
}
