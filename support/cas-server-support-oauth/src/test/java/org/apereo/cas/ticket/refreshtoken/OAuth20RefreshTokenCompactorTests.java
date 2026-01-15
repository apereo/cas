package org.apereo.cas.ticket.refreshtoken;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.registry.TicketCompactor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20RefreshTokenCompactorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OAuthToken")
class OAuth20RefreshTokenCompactorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauth20RefreshTokenTicketCompactor")
    private TicketCompactor<OAuth20RefreshToken> oauth20RefreshTokenTicketCompactor;

    @ParameterizedTest
    @MethodSource("codeProvider")
    void verifyOperation(final Service service, final Authentication authentication,
                         final Set scopes, final String clientId, final OAuth20ResponseTypes responseType,
                         final OAuth20GrantTypes grantType) throws Throwable {
        val registeredService = getRegisteredService("https://code.oauth.org", clientId, "secret-at");
        servicesManager.save(registeredService);
        val token = defaultRefreshTokenFactory.create(service, authentication, null, scopes,
            clientId, null, Map.of(), responseType, grantType);
        assertSame(OAuth20RefreshToken.class, oauth20RefreshTokenTicketCompactor.getTicketType());
        val compacted = oauth20RefreshTokenTicketCompactor.compact(token);
        assertNotNull(compacted);
        val result = (OAuth20RefreshToken) oauth20RefreshTokenTicketCompactor.expand(compacted);
        assertNotNull(result);
        assertEquals(result.getClientId(), token.getClientId());
        assertEquals(result.getScopes(), token.getScopes());
        assertEquals(result.getResponseType(), token.getResponseType());
        assertEquals(result.getGrantType(), token.getGrantType());
        assertEquals(result.getAuthentication().getPrincipal(), token.getAuthentication().getPrincipal());
    }

    static Stream<Arguments> codeProvider() {
        val service = RegisteredServiceTestUtils.getService("https://code.oauth.org");
        val authentication = RegisteredServiceTestUtils.getAuthentication();
        return Stream.of(
            Arguments.of(service, authentication,
                Set.of("Scope1", "Scope2"), "clientid-code",
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE)
        );
    }
}
