package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.code.OAuth20Code;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;
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
public class OAuth20AuthorizationCodeAuthorizationResponseBuilderTests extends AbstractOAuth20Tests {
    private static final String STATE = UUID.randomUUID().toString();
    private static final String NONCE = UUID.randomUUID().toString();

    @Test
    public void verifyOperation() {
        val registeredService = getRegisteredService("example", CLIENT_SECRET, new LinkedHashSet<>());
        servicesManager.save(registeredService);

        val attributes = new HashMap<String, List<Object>>();
        attributes.put(OAuth20Constants.STATE, Collections.singletonList(STATE));
        attributes.put(OAuth20Constants.NONCE, Collections.singletonList(NONCE));
        val authentication = RegisteredServiceTestUtils.getAuthentication(
            RegisteredServiceTestUtils.getPrincipal("casuser"), attributes);
        val holder = AccessTokenRequestDataHolder.builder()
            .clientId(registeredService.getClientId())
            .authentication(authentication)
            .registeredService(registeredService)
            .responseType(OAuth20ResponseTypes.CODE)
            .ticketGrantingTicket(new MockTicketGrantingTicket(authentication))
            .service(RegisteredServiceTestUtils.getService("example"))
            .build();
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://github.com/apereo/cas");
        request.addParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
        val context = new JEEContext(request, new MockHttpServletResponse());
        val mv = oauthAuthorizationCodeResponseBuilder.build(context, registeredService.getClientId(), holder);
        assertTrue(mv.getView() instanceof RedirectView);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.CODE));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.STATE));
        assertTrue(mv.getModel().containsKey(OAuth20Constants.NONCE));

        val code = mv.getModel().get(OAuth20Constants.CODE).toString();
        assertNotNull(ticketRegistry.getTicket(code, OAuth20Code.class));
        assertTrue(oauthAuthorizationCodeResponseBuilder.supports(context));
    }
}
