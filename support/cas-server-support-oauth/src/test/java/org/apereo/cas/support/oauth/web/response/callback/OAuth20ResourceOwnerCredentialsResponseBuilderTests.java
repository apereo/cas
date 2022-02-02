package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20ResourceOwnerCredentialsResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuth")
public class OAuth20ResourceOwnerCredentialsResponseBuilderTests extends AbstractOAuth20Tests {

    @Test
    public void verifyOperation() throws Exception {
        val holder = AccessTokenRequestContext.builder()
            .clientId(CLIENT_ID)
            .service(CoreAuthenticationTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication(
                CoreAuthenticationTestUtils.getPrincipal("casuser")))
            .registeredService(getRegisteredService(CLIENT_ID, CLIENT_SECRET))
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .build();
        assertNotNull(oauthResourceOwnerCredentialsResponseBuilder.build(holder));
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, CLIENT_ID);
        assertNotNull(oauthResourceOwnerCredentialsResponseBuilder.build(registeredService,
            OAuth20ResponseModeTypes.FORM_POST, "https://example.org", Map.of()));
    }

    @Test
    public void verifyModelAndViewPost() throws Exception {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, CLIENT_ID);
        assertNotNull(oauthResourceOwnerCredentialsResponseBuilder.build(registeredService, OAuth20ResponseModeTypes.FORM_POST, "https://example.org", Map.of("key", "value")));
    }

    @Test
    public void verifyModelAndView() throws Exception {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, CLIENT_ID);
        assertNotNull(oauthResourceOwnerCredentialsResponseBuilder.build(registeredService, OAuth20ResponseModeTypes.FORM_POST, "https://example.org", Map.of("key", "value")));
    }
}
