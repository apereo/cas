package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultRefreshTokenFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuth")
public class OAuth20DefaultRefreshTokenFactoryTests extends AbstractOAuth20Tests {
    @Test
    public void verifyOperationWithExpPolicy() {
        val registeredService = getRegisteredService("https://rt.oauth.org", "clientid-rt", "secret-at");
        registeredService.setRefreshTokenExpirationPolicy(
            new DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy("PT100S"));
        servicesManager.save(registeredService);
        val token = oAuthRefreshTokenFactory.create(RegisteredServiceTestUtils.getService("https://rt.oauth.org"),
            RegisteredServiceTestUtils.getAuthentication(),
            new MockTicketGrantingTicket("casuser"),
            Set.of("Scope1", "Scope2"), "clientid-rt", "at-1234567890", Map.of());
        assertNotNull(token);
        assertNotNull(defaultAccessTokenFactory.get(OAuth20RefreshToken.class));
    }
}
