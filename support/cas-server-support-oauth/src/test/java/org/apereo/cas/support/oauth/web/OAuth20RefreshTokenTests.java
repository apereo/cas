package org.apereo.cas.support.oauth.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.junit.Before;
import org.junit.Test;

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
    public void setUp() {
        clearAllServices();
    }

    @Test
    public void verifyTicketGrantingRemovalDoesNotRemoveAccessToken() throws Exception {
        final OAuthRegisteredService service = addRegisteredService();
        service.setGenerateRefreshToken(true);
        service.setJsonFormat(true);

        final Pair<String, String> result = internalVerifyClientOK(service, true, true);

        final AccessToken at = this.ticketRegistry.getTicket(result.getKey(), AccessToken.class);
        assertNotNull(at);
        assertNotNull(at.getTicketGrantingTicket());

        this.ticketRegistry.deleteTicket(at.getTicketGrantingTicket().getId());
        final AccessToken at2 = this.ticketRegistry.getTicket(at.getId(), AccessToken.class);
        assertNotNull(at2);

        final RefreshToken rt = this.ticketRegistry.getTicket(result.getRight(), RefreshToken.class);
        assertNotNull(rt);

        final Pair<AccessToken, RefreshToken> result2 = internalVerifyRefreshTokenOk(service, true, rt, createPrincipal());
        assertNotNull(result2.getKey());
    }

}
