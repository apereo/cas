package org.apereo.cas.services;

import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceAccessStrategyUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Utility")
class RegisteredServiceAccessStrategyUtilsTests {

    @Test
    void verifyExpired() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        service.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(false,
            LocalDate.now(ZoneOffset.UTC).minusDays(1)));
        assertThrows(UnauthorizedServiceException.class, () ->
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(RegisteredServiceTestUtils.getService(), service));
    }

    @Test
    void verifySsoAccess() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication());
        when(tgt.getProxiedBy()).thenReturn(RegisteredServiceTestUtils.getService());
        assertThrows(UnauthorizedSsoServiceException.class, () ->
            RegisteredServiceAccessStrategyUtils.ensureServiceSsoAccessIsAllowed(service, RegisteredServiceTestUtils.getService(), tgt, false));
    }

    @Test
    void verifySsoAccessDisabledAllowsAccessWithCredentials() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getAuthentication()).thenReturn(RegisteredServiceTestUtils.getAuthentication());
        when(tgt.getCountOfUses()).thenReturn(0);
        
        assertDoesNotThrow(() -> RegisteredServiceAccessStrategyUtils.ensureServiceSsoAccessIsAllowed(service,
            RegisteredServiceTestUtils.getService(), tgt, true));
    }

    @Test
    void verifyPrincipalAccess() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        val authentication = RegisteredServiceTestUtils.getAuthentication();
        assertThrows(PrincipalException.class, () ->
            RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(
                RegisteredServiceTestUtils.getService(), service, authentication.getPrincipal().getId(),
                (Map) CollectionUtils.merge(authentication.getAttributes(), authentication.getPrincipal().getAttributes())));
    }

}
