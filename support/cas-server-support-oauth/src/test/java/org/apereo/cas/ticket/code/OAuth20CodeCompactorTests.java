package org.apereo.cas.ticket.code;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.compact.TicketCompactor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
                         final OAuth20GrantTypes grantType)
            throws Throwable {
        val registeredService = getRegisteredService("https://code.oauth.org", clientId, "secret-at");
        servicesManager.save(registeredService);
        val token = defaultOAuthCodeFactory.create(service, authentication, tgt,
            scopes, codeChallenge, codeChallengeMethod, clientId,
            claims, responseType, grantType);
        val authorizationDetails = List.of(new LinkedHashMap<String, Object>(Map.of("type", "openid_credential",
            "credential_configuration_id", "UniversityDegreeCredential")));
        token.setAuthorizationDetails(authorizationDetails);
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
        assertEquals(authorizationDetails, result.getAuthorizationDetails());
        assertEquals(token.getAuthentication().getAttributes().get(OAuth20Constants.DPOP_JKT),
            result.getAuthentication().getAttributes().get(OAuth20Constants.DPOP_JKT));

    }

    static Stream<Arguments> codeProvider() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val service = RegisteredServiceTestUtils.getService("https://code.oauth.org");
        val authentication = RegisteredServiceTestUtils.getAuthentication();

        return Stream.of(
            Arguments.of(service, RegisteredServiceTestUtils.getAuthentication("casuser",
                    Map.of(OAuth20Constants.DPOP_JKT, List.of("NzbLsXh8uDCcd-6MNwXF4W_7noWXFZAfHkxZsRGC9Xs"))),
                tgt, Set.of("Scope1", "Scope2"), "code-challenge", "plain",
                "clientid-code", Map.of(), OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE),
            Arguments.of(service, authentication,
                tgt, Set.of(), null, null,
                "clientid", Map.of(), OAuth20ResponseTypes.ID_TOKEN, OAuth20GrantTypes.CLIENT_CREDENTIALS),
            Arguments.of(service, RegisteredServiceTestUtils.getAuthentication("CN=Jane Doe,OU=Staff:Faculty,O=Example"),
                tgt, Set.of("openid,profile", "email|phone", "offline#access"), "challenge,1:2#3|4", "S256",
                "client,id:1", Map.of(), OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE)
        );
    }
}
