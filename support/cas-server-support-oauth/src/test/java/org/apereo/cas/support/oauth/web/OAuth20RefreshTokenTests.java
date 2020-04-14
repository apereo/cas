package org.apereo.cas.support.oauth.web;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link OAuth20AccessTokenEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Tag("OAuth")
public class OAuth20RefreshTokenTests extends AbstractOAuth20Tests {

    @BeforeEach
    public void initialize() {
        clearAllServices();
    }

    @Test
    public void verifyTicketGrantingRemovalDoesNotRemoveAccessToken() throws Exception {
        val service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setRenewRefreshToken(true);

        val result = assertClientOK(service, true);

        val at = this.ticketRegistry.getTicket(result.getKey(), OAuth20AccessToken.class);
        assertNotNull(at);
        assertNotNull(at.getTicketGrantingTicket());

        this.ticketRegistry.deleteTicket(at.getTicketGrantingTicket().getId());
        val at2 = this.ticketRegistry.getTicket(at.getId(), OAuth20AccessToken.class);
        assertNotNull(at2);

        val rt = this.ticketRegistry.getTicket(result.getRight(), OAuth20RefreshToken.class);
        assertNotNull(rt);

        val result2 = assertRefreshTokenOk(service, rt, createPrincipal());
        assertNotNull(result2.getKey());
    }

    @Test
    public void verifyRenewingRefreshToken() throws Exception {
        val service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setRenewRefreshToken(true);

        val result = assertClientOK(service, true);

        val at = this.ticketRegistry.getTicket(result.getLeft(), OAuth20AccessToken.class);
        assertNotNull(at);
        assertNotNull(at.getTicketGrantingTicket());

        val rt = this.ticketRegistry.getTicket(result.getRight(), OAuth20RefreshToken.class);
        assertNotNull(rt);

        val result2 = assertRefreshTokenOk(service, rt, createPrincipal());
        assertNotNull(result2.getLeft());
        assertNotNull(result2.getRight());

        val rt2 = result2.getRight();
        assertNotEquals(rt.getId(), rt2.getId());

        val oldRt = this.ticketRegistry.getTicket(result.getRight(), ticket -> true);
        assertNull(oldRt);
    }
}
