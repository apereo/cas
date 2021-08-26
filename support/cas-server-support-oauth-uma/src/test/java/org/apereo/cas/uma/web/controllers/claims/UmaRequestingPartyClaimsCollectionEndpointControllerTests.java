package org.apereo.cas.uma.web.controllers.claims;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link UmaRequestingPartyClaimsCollectionEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
public class UmaRequestingPartyClaimsCollectionEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    public void verifyOp() {
        val id = UUID.randomUUID().toString();
        val service = getRegisteredService(id, "secret");
        servicesManager.save(service);

        val results = authenticateUmaRequestWithProtectionScope();

        val ticketId = UUID.randomUUID().toString();
        val permissionTicket = mock(UmaPermissionTicket.class);
        when(permissionTicket.getId()).thenReturn(ticketId);
        when(permissionTicket.isExpired()).thenReturn(Boolean.FALSE);
        when(permissionTicket.getClaims()).thenReturn(new HashMap<>());
        ticketRegistry.addTicket(permissionTicket);

        val view = umaRequestingPartyClaimsCollectionEndpointController.getClaims(id,
            service.getServiceId(), ticketId,
            "state", results.getLeft(), results.getMiddle());
        assertTrue(view instanceof RedirectView);
    }

    @Test
    public void verifyInvalidRedirect() {
        val id = UUID.randomUUID().toString();
        val service = getRegisteredService(id, "secret");
        servicesManager.save(service);

        val results = authenticateUmaRequestWithProtectionScope();

        val ticketId = UUID.randomUUID().toString();
        val permissionTicket = mock(UmaPermissionTicket.class);
        when(permissionTicket.getId()).thenReturn(ticketId);
        when(permissionTicket.isExpired()).thenReturn(Boolean.FALSE);
        when(permissionTicket.getClaims()).thenReturn(new HashMap<>());
        ticketRegistry.addTicket(permissionTicket);

        assertThrows(UnauthorizedServiceException.class,
            () -> umaRequestingPartyClaimsCollectionEndpointController.getClaims(id,
                "bad-redirect", ticketId,
                "state", results.getLeft(), results.getMiddle()));
    }

    @Test
    public void verifyInvalidTicket() {
        val id = UUID.randomUUID().toString();
        val service = getRegisteredService(id, "secret");
        servicesManager.save(service);

        val results = authenticateUmaRequestWithProtectionScope();

        val ticketId = UUID.randomUUID().toString();
        val permissionTicket = mock(UmaPermissionTicket.class);
        when(permissionTicket.getId()).thenReturn(ticketId);
        when(permissionTicket.isExpired()).thenReturn(Boolean.TRUE);
        when(permissionTicket.getClaims()).thenReturn(new HashMap<>());
        ticketRegistry.addTicket(permissionTicket);

        assertThrows(InvalidTicketException.class,
            () -> umaRequestingPartyClaimsCollectionEndpointController.getClaims(id,
                service.getServiceId(), ticketId,
                "state", results.getLeft(), results.getMiddle()));
    }
}
