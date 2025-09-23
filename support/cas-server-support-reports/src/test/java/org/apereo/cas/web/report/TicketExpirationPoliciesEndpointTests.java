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
@TestPropertySource(properties = "management.endpoint.ticketExpirationPolicies.access=UNRESTRICTED")
@Tag("ActuatorEndpoint")
class TicketExpirationPoliciesEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("ticketExpirationPoliciesEndpoint")
    private TicketExpirationPoliciesEndpoint ticketExpirationPoliciesEndpoint;

    @Test
    void verifyOperation() {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val tgtPolicy = new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy().setMaxTimeToLiveInSeconds(10);
        service.setTicketGrantingTicketExpirationPolicy(tgtPolicy);
        service.setServiceTicketExpirationPolicy(new DefaultRegisteredServiceServiceTicketExpirationPolicy(10, "PT10S"));
        service.setProxyTicketExpirationPolicy(new DefaultRegisteredServiceProxyTicketExpirationPolicy(10, "PT10S"));
        service.setProxyGrantingTicketExpirationPolicy(new DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy(10));
        servicesManager.save(service);

        assertFalse(ticketExpirationPoliciesEndpoint.getExpirationPolicyBuilders().isEmpty());
        assertNotNull(ticketExpirationPoliciesEndpoint.getServicesManagerProvider().getObject());
        assertNotNull(ticketExpirationPoliciesEndpoint.getWebApplicationServiceFactory());
        var results = ticketExpirationPoliciesEndpoint.handle(service.getServiceId());
        assertFalse(results.isEmpty());

        results = ticketExpirationPoliciesEndpoint.handle(String.valueOf(service.getId()));
        assertFalse(results.isEmpty());
    }
}
