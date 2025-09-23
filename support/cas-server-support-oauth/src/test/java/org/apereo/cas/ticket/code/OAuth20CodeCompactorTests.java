package org.apereo.cas.ticket.code;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketCompactor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20CodeCompactorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OAuthToken")
class OAuth20CodeCompactorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauth20CodeTicketCompactor")
    private TicketCompactor<OAuth20Code> oauth20CodeTicketCompactor;

    @ParameterizedTest
    @MethodSource("codeProvider")
    void verifyOperation(final Service service, final Authentication authentication,
                         final Ticket tgt, final Set scopes, final String codeChallenge,
                         final String codeChallengeMethod,
                         final String clientId, final Map claims,
                         final OAuth20ResponseTypes responseType,
                         final OAuth20GrantTypes grantType) throws Throwable {
        val registeredService = getRegisteredService("https://code.oauth.org", clientId, "secret-at");
        servicesManager.save(registeredService);
        val token = defaultOAuthCodeFactory.create(service, authentication, tgt,
            scopes, codeChallenge, codeChallengeMethod, clientId,
            claims, responseType, grantType);
        assertSame(OAuth20Code.class, oauth20CodeTicketCompactor.getTicketType());
        val compacted = oauth20CodeTicketCompactor.compact(token);
        assertNotNull(compacted);
        val result = (OAuth20Code) oauth20CodeTicketCompactor.expand(compacted);
        assertNotNull(result);
        assertEquals(result.getClientId(), token.getClientId());
        assertEquals(result.getScopes(), token.getScopes());
        assertEquals(result.getCodeChallenge(), token.getCodeChallenge());
        assertEquals(result.getCodeChallengeMethod(), token.getCodeChallengeMethod());
        assertEquals(result.getResponseType(), token.getResponseType());
        assertEquals(result.getGrantType(), token.getGrantType());
        assertEquals(result.getAuthentication().getPrincipal(), token.getAuthentication().getPrincipal());

    }

    static Stream<Arguments> codeProvider() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService("https://code.oauth.org");
        val authentication = RegisteredServiceTestUtils.getAuthentication();

        return Stream.of(
            Arguments.of(service, authentication,
                tgt, Set.of("Scope1", "Scope2"), "code-challenge", "plain",
                "clientid-code", Map.of(), OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE),
            Arguments.of(service, authentication,
                tgt, Set.of(), null, null,
                "clientid", Map.of(), OAuth20ResponseTypes.ID_TOKEN, OAuth20GrantTypes.CLIENT_CREDENTIALS)
        );
    }
}
