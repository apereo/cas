package org.apereo.cas.web.report;

import org.apereo.cas.services.DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProxyTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceServiceTicketExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TicketExpirationPoliciesEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = "management.endpoint.ticketExpirationPolicies.enabled=true")
@Tag("ActuatorEndpoint")
public class TicketExpirationPoliciesEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("ticketExpirationPoliciesEndpoint")
    private TicketExpirationPoliciesEndpoint ticketExpirationPoliciesEndpoint;

    @Test
    public void verifyOperation() throws Exception {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        service.setTicketGrantingTicketExpirationPolicy(new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy(10));
        service.setServiceTicketExpirationPolicy(new DefaultRegisteredServiceServiceTicketExpirationPolicy(10, "PT10S"));
        service.setProxyTicketExpirationPolicy(new DefaultRegisteredServiceProxyTicketExpirationPolicy(10, "PT10S"));
        service.setProxyGrantingTicketExpirationPolicy(new DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy(10));
        servicesManager.save(service);

        assertFalse(ticketExpirationPoliciesEndpoint.getExpirationPolicyBuilders().isEmpty());
        assertNotNull(ticketExpirationPoliciesEndpoint.getServicesManager());
        assertNotNull(ticketExpirationPoliciesEndpoint.getWebApplicationServiceFactory());
        var results = ticketExpirationPoliciesEndpoint.handle(service.getServiceId());
        assertFalse(results.isEmpty());

        results = ticketExpirationPoliciesEndpoint.handle(String.valueOf(service.getId()));
        assertFalse(results.isEmpty());
    }
}
