package org.apereo.cas.services;

import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceAccessStrategyUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class RegisteredServiceAccessStrategyUtilsTests {

    @Test
    public void verifyExpired() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        service.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(false,
            LocalDate.now(ZoneOffset.UTC).minusDays(1)));
        assertThrows(UnauthorizedServiceException.class, () ->
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(RegisteredServiceTestUtils.getService().getId(), service));
    }

    @Test
    public void verifySsoAccess() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication());
        when(tgt.getProxiedBy()).thenReturn(RegisteredServiceTestUtils.getService());
        assertThrows(UnauthorizedSsoServiceException.class, () ->
            RegisteredServiceAccessStrategyUtils.ensureServiceSsoAccessIsAllowed(service, RegisteredServiceTestUtils.getService(), tgt, false));
    }

    @Test
    public void verifyPrincipalAccess() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val st = mock(ServiceTicket.class);
        when(st.getService()).thenReturn(RegisteredServiceTestUtils.getService());
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication());
        assertThrows(PrincipalException.class, () ->
            RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(st, service, tgt, false));
    }

}
