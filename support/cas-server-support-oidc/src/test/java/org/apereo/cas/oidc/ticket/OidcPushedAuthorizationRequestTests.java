package org.apereo.cas.oidc.ticket;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcPushedAuthorizationRequestTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
class OidcPushedAuthorizationRequestTests extends AbstractOidcTests {
    @Autowired
    @Qualifier(TicketSerializationManager.BEAN_NAME)
    private TicketSerializationManager ticketSerializationManager;

    @Test
    void verifyOperation() throws Throwable {
        val profile = new CommonProfile();
        profile.setId("casuser");

        val registeredService = getOidcRegisteredService();
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
        verifySerialization(ticket);
    }

    private void verifySerialization(final Ticket ticket) {
        val serialized = ticketSerializationManager.serializeTicket(ticket);
        assertNotNull(serialized);
        val deserialized = ticketSerializationManager.deserializeTicket(serialized, ticket.getClass());
        assertNotNull(deserialized);
        assertEquals(deserialized, ticket);
    }
}
