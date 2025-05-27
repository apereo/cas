package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.code.OAuth20Code;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20AuthorizationCodeAuthorizationResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class OAuth20AuthorizationCodeAuthorizationResponseBuilderTests extends AbstractOAuth20Tests {
    private static final String STATE = UUID.randomUUID().toString();

    private static final String NONCE = UUID.randomUUID().toString();

    @Test
    void verifyOperation() throws Throwable {
        val registeredService = getRegisteredService("example", CLIENT_SECRET, new LinkedHashSet<>());
        servicesManager.save(registeredService);

        val attributes = new HashMap<String, List<Object>>();
        attributes.put(OAuth20Constants.STATE, List.of(STATE));
        attributes.put(OAuth20Constants.NONCE, List.of(NONCE));
        val authentication = RegisteredServiceTestUtils.getAuthentication(
            RegisteredServiceTestUtils.getPrincipal("casuser"), attributes);
        val holder = AccessTokenRequestContext.builder()
            .clientId(registeredService.getClientId())
            .authentication(authentication)
            .registeredService(registeredService)
            .responseType(OAuth20ResponseTypes.CODE)
            .ticketGrantingTicket(new MockTicketGrantingTicket(authentication))
            .service(RegisteredServiceTestUtils.getService("example"))
            .redirectUri("https://github.com/apereo/cas")
            .build();

        val mv = oauthAuthorizationCodeResponseBuilder.build(holder);
        assertInstanceOf(RedirectView.class, mv.getView());
        assertTrue(mv.getModel().containsKey(OAuth20Constants.CODE));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.STATE));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.NONCE));

        val code = mv.getModel().get(OAuth20Constants.CODE).toString();
        assertNotNull(ticketRegistry.getTicket(code, OAuth20Code.class));

        val authzRequest = OAuth20AuthorizationRequest.builder()
            .responseType(OAuth20ResponseTypes.CODE.getType())
            .clientId(holder.getClientId())
            .build();
        assertTrue(oauthAuthorizationCodeResponseBuilder.supports(authzRequest));
    }
}
