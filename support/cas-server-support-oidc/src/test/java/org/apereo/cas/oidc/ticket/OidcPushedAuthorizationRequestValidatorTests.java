package org.apereo.cas.oidc.ticket;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcPushedAuthorizationRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
public class OidcPushedAuthorizationRequestValidatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcPushedAuthorizationRequestValidator")
    private OAuth20AuthorizationRequestValidator oidcPushedAuthorizationRequestValidator;

    @Test
    public void verifyOperation() throws Exception {
        val registeredService = getOidcRegisteredService();
        val profile = new CommonProfile();
        profile.setId("casTest");
        val holder = AccessTokenRequestContext.builder()
            .clientId(registeredService.getClientId())
            .service(RegisteredServiceTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .userProfile(profile)
            .build();
        val factory = (OidcPushedAuthorizationRequestFactory) defaultTicketFactory.get(OidcPushedAuthorizationRequest.class);
        val ticket = factory.create(holder);
        assertNotNull(ticket);
        ticketRegistry.addTicket(ticket);
        
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, holder.getClientId());
        request.addParameter(OidcConstants.REQUEST_URI, ticket.getId());

        val context = new JEEContext(request, new MockHttpServletResponse());
        assertTrue(oidcPushedAuthorizationRequestValidator.supports(context));
        assertTrue(oidcPushedAuthorizationRequestValidator.validate(context));
        assertEquals(0, oidcPushedAuthorizationRequestValidator.getOrder());
    }
}
