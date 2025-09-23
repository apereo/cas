package org.apereo.cas.ticket.factory;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultTicketGrantingTicketFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Tickets")
class DefaultTicketGrantingTicketFactoryTests extends BaseTicketFactoryTests {

    @Test
    void verifyNoExpirationPolicy() throws Throwable {
        val service = RegisteredServiceTestUtils.getService("noExpirationPolicy");
        val factory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val tgt = factory.create(RegisteredServiceTestUtils.getAuthentication(), service);
        val seconds = Beans.newDuration(casProperties.getTicket().getTgt().getPrimary().getMaxTimeToLiveInSeconds()).toSeconds();
        assertEquals(seconds, tgt.getExpirationPolicy().getTimeToLive());
    }

    @Test
    void verifyExpirationPolicyPerAuthenticationAsSeconds() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("someTgtExpirationPolicy", CasRegisteredService.class);
        servicesManager.save(registeredService);
        val factory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser",
            Map.of(AuthenticationManager.AUTHENTICATION_SESSION_TIMEOUT_ATTRIBUTE, List.of(600)));
        val tgt = factory.create(authentication, RegisteredServiceTestUtils.getService(registeredService.getServiceId()));
        assertEquals(600, tgt.getExpirationPolicy().getTimeToLive());
    }

    @Test
    void verifyExpirationPolicyPerAuthenticationAsDuration() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("durationTgtExpirationPolicy", CasRegisteredService.class);
        servicesManager.save(registeredService);
        val factory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser",
            Map.of(AuthenticationManager.AUTHENTICATION_SESSION_TIMEOUT_ATTRIBUTE, List.of("PT10S")));
        val tgt = factory.create(authentication, RegisteredServiceTestUtils.getService(registeredService.getServiceId()));
        assertEquals(10, tgt.getExpirationPolicy().getTimeToLive());
    }

    @Test
    void verifyCustomExpirationPolicy() throws Throwable {
        val defaultSvc = RegisteredServiceTestUtils.getRegisteredService("customTgtExpirationPolicy", CasRegisteredService.class);
        val policy = new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy().setMaxTimeToLiveInSeconds(120);
        defaultSvc.setTicketGrantingTicketExpirationPolicy(policy);
        servicesManager.save(defaultSvc);

        val service = RegisteredServiceTestUtils.getService("customTgtExpirationPolicy");
        val factory = (TicketGrantingTicketFactory) this.ticketFactory.get(TicketGrantingTicket.class);
        val tgt = factory.create(RegisteredServiceTestUtils.getAuthentication(), service);
        assertEquals(120, tgt.getExpirationPolicy().getTimeToLive());
    }
}
