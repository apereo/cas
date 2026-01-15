package org.apereo.cas.support.oauth.web.response.callback;

import module java.base;
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
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20ResourceOwnerCredentialsResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuth")
class OAuth20ResourceOwnerCredentialsResponseBuilderTests extends AbstractOAuth20Tests {

    @Test
    void verifyOperation() throws Throwable {
        val clientId = addRegisteredService().getClientId();
        val holder = AccessTokenRequestContext.builder()
            .clientId(clientId)
            .service(CoreAuthenticationTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal("casuser")))
            .registeredService(getRegisteredService(clientId, CLIENT_SECRET))
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .build();
        assertNotNull(oauthResourceOwnerCredentialsResponseBuilder.build(holder));
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        assertNotNull(oauthResourceOwnerCredentialsResponseBuilder.build(registeredService,
            OAuth20ResponseModeTypes.FORM_POST, "https://example.org", Map.of()));
    }

    @Test
    void verifyModelAndViewPost() throws Throwable {
        val clientId = addRegisteredService().getClientId();
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        assertNotNull(oauthResourceOwnerCredentialsResponseBuilder.build(registeredService, OAuth20ResponseModeTypes.FORM_POST, "https://example.org", Map.of("key", "value")));
    }

    @Test
    void verifyModelAndView() throws Throwable {
        val clientId = addRegisteredService().getClientId();
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        assertNotNull(oauthResourceOwnerCredentialsResponseBuilder.build(registeredService, OAuth20ResponseModeTypes.FORM_POST, "https://example.org", Map.of("key", "value")));
    }
}
