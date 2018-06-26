package org.apereo.cas.support.oauth.web;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 * This class tests the {@link OAuth20AccessTokenEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Slf4j
public class OAuth20RefreshTokenTests extends AbstractOAuth20Tests {

    @Before
    public void initialize() {
        clearAllServices();
    }

    @Test
    public void verifyTicketGrantingRemovalDoesNotRemoveAccessToken() throws Exception {
        final var service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);

        final var result = internalVerifyClientOK(service, true, true);

        final var at = this.ticketRegistry.getTicket(result.getKey(), AccessToken.class);
        assertNotNull(at);
        assertNotNull(at.getTicketGrantingTicket());

        this.ticketRegistry.deleteTicket(at.getTicketGrantingTicket().getId());
        final var at2 = this.ticketRegistry.getTicket(at.getId(), AccessToken.class);
        assertNotNull(at2);

        final var rt = this.ticketRegistry.getTicket(result.getRight(), RefreshToken.class);
        assertNotNull(rt);

        final var result2 = internalVerifyRefreshTokenOk(service, true, rt, createPrincipal());
        assertNotNull(result2.getKey());
    }

}
