package org.apereo.cas.ticket.accesstoken;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.registry.compact.CompactTicketAuthentication;
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
 * This is {@link OAuth20AccessTokenCompactorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OAuthToken")
class OAuth20AccessTokenCompactorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauth20AccessTokenTicketCompactor")
    private TicketCompactor<OAuth20AccessToken> oauth20AccessTokenTicketCompactor;

    @ParameterizedTest
    @MethodSource("codeProvider")
    void verifyOperation(final Service service, final Authentication authentication,
                         final Set scopes, final String clientId, final OAuth20ResponseTypes responseType,
                         final OAuth20GrantTypes grantType)
            throws Throwable {
        val registeredService = getRegisteredService("https://code.oauth.org", clientId, "secret-at");
        servicesManager.save(registeredService);
        val token = defaultAccessTokenFactory.create(service,
            authentication, scopes, clientId, responseType, grantType);
        token.setCredentialConfigurationIds(List.of("UniversityDegreeCredential"));
        val authorizationDetails = List.of(new LinkedHashMap<String, Object>(Map.of("type", "openid_credential",
            "credential_configuration_id", "UniversityDegreeCredential",
            "credential_identifiers", List.of("UniversityDegreeCredential"))));
        token.setAuthorizationDetails(authorizationDetails);
        assertSame(OAuth20AccessToken.class, oauth20AccessTokenTicketCompactor.getTicketType());
        val compacted = oauth20AccessTokenTicketCompactor.compact(token);
        assertNotNull(compacted);
        val result = (OAuth20AccessToken) oauth20AccessTokenTicketCompactor.expand(compacted);
        assertNotNull(result);
        assertEquals(result.getClientId(), token.getClientId());
        assertEquals(result.getScopes(), token.getScopes());

        assertEquals(result.getResponseType(), token.getResponseType());
        assertEquals(result.getGrantType(), token.getGrantType());
        assertEquals(List.of("UniversityDegreeCredential"), result.getCredentialConfigurationIds());
        assertEquals(authorizationDetails, result.getAuthorizationDetails());
        assertEquals(result.getAuthentication().getPrincipal(), token.getAuthentication().getPrincipal());
        assertEquals(List.of("SAML2Client"), result.getAuthentication().getAttributes().get(CompactTicketAuthentication.CLIENT_NAME_ATTRIBUTE));
        assertEquals(List.of("NzbLsXh8uDCcd-6MNwXF4W_7noWXFZAfHkxZsRGC9Xs"),
            result.getAuthentication().getAttributes().get(OAuth20Constants.DPOP_CONFIRMATION));
        assertEquals(token.getCreationTime().toEpochSecond(), result.getCreationTime().toEpochSecond());
        assertEquals(token.getExpirationPolicy().toMaximumExpirationTime(token).toEpochSecond(),
            result.getExpirationPolicy().toMaximumExpirationTime(result).toEpochSecond());
    }

    static Stream<Arguments> codeProvider() {
        val service = RegisteredServiceTestUtils.getService("https://code.oauth.org");
        val authentication = RegisteredServiceTestUtils.getAuthentication(RegisteredServiceTestUtils.getPrincipal("casuser"),
            new HashMap<>(Map.of(CompactTicketAuthentication.CLIENT_NAME_ATTRIBUTE, List.of("SAML2Client"),
                OAuth20Constants.DPOP_CONFIRMATION, List.of("NzbLsXh8uDCcd-6MNwXF4W_7noWXFZAfHkxZsRGC9Xs"))));
        return Stream.of(
            Arguments.of(service, authentication,
                Set.of("Scope1", "Scope2"), "clientid-code",
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE)
        );
    }
}
