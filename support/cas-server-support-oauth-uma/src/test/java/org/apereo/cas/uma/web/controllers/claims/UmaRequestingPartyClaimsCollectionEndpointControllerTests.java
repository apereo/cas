package org.apereo.cas.uma.web.controllers.claims;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link UmaRequestingPartyClaimsCollectionEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
class UmaRequestingPartyClaimsCollectionEndpointControllerTests extends BaseUmaEndpointControllerTests {
    @Test
    void verifyOp() throws Throwable {
        val id = UUID.randomUUID().toString();
        val service = getRegisteredService(id, "secret");
        servicesManager.save(service);

        val results = authenticateUmaRequestWithProtectionScope();

        val ticketId = UUID.randomUUID().toString();
        val permissionTicket = getUmaPermissionTicket(ticketId);

        ticketRegistry.addTicket(permissionTicket);

        val result = performUmaRequest(HttpMethod.GET, OAuth20Constants.UMA_CLAIMS_COLLECTION_URL,
            CollectionUtils.wrap(
                OAuth20Constants.CLIENT_ID, id,
                OAuth20Constants.REDIRECT_URI, service.getServiceId(),
                "ticket", ticketId,
                OAuth20Constants.STATE, "state"
            ), results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertNotNull(result.getResponse().getRedirectedUrl());
    }

    private static UmaPermissionTicket getUmaPermissionTicket(final String ticketId) {
        val permissionTicket = mock(UmaPermissionTicket.class);
        when(permissionTicket.getId()).thenReturn(ticketId);
        when(permissionTicket.isExpired()).thenReturn(Boolean.FALSE);
        when(permissionTicket.getClaims()).thenReturn(new HashMap<>());
        when(permissionTicket.getCreationTime()).thenReturn(ZonedDateTime.now(Clock.systemUTC()));
        when(permissionTicket.getExpirationPolicy()).thenReturn(NeverExpiresExpirationPolicy.INSTANCE);
        return permissionTicket;
    }

    @Test
    void verifyInvalidRedirect() throws Throwable {
        val id = UUID.randomUUID().toString();
        val service = getRegisteredService(id, "secret");
        servicesManager.save(service);

        val results = authenticateUmaRequestWithProtectionScope();

        val ticketId = UUID.randomUUID().toString();
        val permissionTicket = getUmaPermissionTicket(ticketId);
        ticketRegistry.addTicket(permissionTicket);

        val result = performUmaRequest(HttpMethod.GET, OAuth20Constants.UMA_CLAIMS_COLLECTION_URL,
            CollectionUtils.wrap(
                OAuth20Constants.CLIENT_ID, id,
                OAuth20Constants.REDIRECT_URI, "bad-redirect",
                "ticket", ticketId,
                OAuth20Constants.STATE, "state"
            ), results.getLeft(), results.getMiddle());
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    void verifyInvalidTicket() throws Throwable {
        val id = UUID.randomUUID().toString();
        val service = getRegisteredService(id, "secret");
        servicesManager.save(service);

        val results = authenticateUmaRequestWithProtectionScope();

        val ticketId = UUID.randomUUID().toString();
        val permissionTicket = getUmaPermissionTicket(ticketId);
        when(permissionTicket.isExpired()).thenReturn(Boolean.TRUE);
        ticketRegistry.addTicket(permissionTicket);

        val exception = assertThrows(RuntimeException.class,
            () -> performUmaRequest(HttpMethod.GET, OAuth20Constants.UMA_CLAIMS_COLLECTION_URL,
                CollectionUtils.wrap(
                    OAuth20Constants.CLIENT_ID, id,
                    OAuth20Constants.REDIRECT_URI, service.getServiceId(),
                    "ticket", ticketId,
                    OAuth20Constants.STATE, "state"
                ), results.getLeft(), results.getMiddle()));
        assertTrue(exception.getMessage().contains("INVALID_TICKET"));
        assertNotNull(exception.getCause());
    }
}
