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

import java.io.Serial;
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
        val tgt = factory.create(RegisteredServiceTestUtils.getAuthentication(), service, TicketGrantingTicket.class);
        val seconds = Beans.newDuration(casProperties.getTicket().getTgt().getPrimary().getMaxTimeToLiveInSeconds()).toSeconds();
        assertEquals(seconds, tgt.getExpirationPolicy().getTimeToLive());
    }

    @Test
    void verifyBadTicketType() throws Throwable {
        val service = RegisteredServiceTestUtils.getService("noExpirationPolicy");
        val factory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        assertThrows(ClassCastException.class,
            () -> factory.create(RegisteredServiceTestUtils.getAuthentication(), service, BaseMockTicketGrantingTicket.class));
    }

    @Test
    void verifyExpirationPolicyPerAuthenticationAsSeconds() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("someTgtExpirationPolicy", CasRegisteredService.class);
        servicesManager.save(registeredService);
        val factory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser",
            Map.of(AuthenticationManager.AUTHENTICATION_SESSION_TIMEOUT_ATTRIBUTE, List.of(600)));
        val tgt = factory.create(authentication, RegisteredServiceTestUtils.getService(registeredService.getServiceId()), TicketGrantingTicket.class);
        assertEquals(600, tgt.getExpirationPolicy().getTimeToLive());
    }

    @Test
    void verifyExpirationPolicyPerAuthenticationAsDuration() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("durationTgtExpirationPolicy", CasRegisteredService.class);
        servicesManager.save(registeredService);
        val factory = (TicketGrantingTicketFactory) ticketFactory.get(TicketGrantingTicket.class);
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser",
            Map.of(AuthenticationManager.AUTHENTICATION_SESSION_TIMEOUT_ATTRIBUTE, List.of("PT10S")));
        val tgt = factory.create(authentication, RegisteredServiceTestUtils.getService(registeredService.getServiceId()), TicketGrantingTicket.class);
        assertEquals(10, tgt.getExpirationPolicy().getTimeToLive());
    }

    @Test
    void verifyCustomExpirationPolicy() throws Throwable {
        val defaultSvc = RegisteredServiceTestUtils.getRegisteredService("customTgtExpirationPolicy", CasRegisteredService.class);
        defaultSvc.setTicketGrantingTicketExpirationPolicy(
            new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy(120));
        servicesManager.save(defaultSvc);

        val service = RegisteredServiceTestUtils.getService("customTgtExpirationPolicy");
        val factory = (TicketGrantingTicketFactory) this.ticketFactory.get(TicketGrantingTicket.class);
        val tgt = factory.create(RegisteredServiceTestUtils.getAuthentication(), service, TicketGrantingTicket.class);
        assertEquals(120, tgt.getExpirationPolicy().getTimeToLive());
    }

    abstract static class BaseMockTicketGrantingTicket implements TicketGrantingTicket {
        @Serial
        private static final long serialVersionUID = 6712185629825357896L;
    }

}
