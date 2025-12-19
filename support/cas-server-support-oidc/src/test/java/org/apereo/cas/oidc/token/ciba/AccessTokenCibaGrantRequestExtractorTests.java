package org.apereo.cas.oidc.token.ciba;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * This is {@link AccessTokenCibaGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDC")
class AccessTokenCibaGrantRequestExtractorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcCibaAccessTokenGrantRequestExtractor")
    private AccessTokenGrantRequestExtractor oidcCibaAccessTokenGrantRequestExtractor;

    static Stream<Arguments> testScenarios() {
        return Stream.of(
            arguments(OidcBackchannelTokenDeliveryModes.POLL.name()),
            arguments(OidcBackchannelTokenDeliveryModes.PING.name()),
            arguments(OidcBackchannelTokenDeliveryModes.PUSH.name()),
            arguments(StringUtils.EMPTY)
        );
    }

    @ParameterizedTest
    @MethodSource("testScenarios")
    void verifyOperation(final String mode) throws Throwable {
        assertTrue(oidcCibaAccessTokenGrantRequestExtractor.requestMustBeAuthenticated());
        assertEquals(OAuth20GrantTypes.CIBA, oidcCibaAccessTokenGrantRequestExtractor.getGrantType());
        assertEquals(OAuth20ResponseTypes.NONE, oidcCibaAccessTokenGrantRequestExtractor.getResponseType());

        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(mode);
        registeredService.setSupportedGrantTypes(Set.of(OAuth20GrantTypes.CIBA.getType()));
        registeredService.setBackchannelClientNotificationEndpoint("https://localhost:1234");
        servicesManager.save(registeredService);

        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString());
        val cibaRequest = newCibaRequest(registeredService, principal);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.CIBA.getType());
        request.addParameter(OidcConstants.AUTH_REQ_ID, cibaRequest.getEncodedId());

        val context = new JEEContext(request, new MockHttpServletResponse());
        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(registeredService.getClientId());
        profile.addAttribute(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        val profileManager = new ProfileManager(context, oauthDistributedSessionStore);
        profileManager.save(true, profile, false);

        assertTrue(oidcCibaAccessTokenGrantRequestExtractor.supports(context));
        assertThrows(InvalidCibaRequestException.class, () -> oidcCibaAccessTokenGrantRequestExtractor.extract(context));

        if (mode.equalsIgnoreCase(OidcBackchannelTokenDeliveryModes.POLL.name()) || mode.equalsIgnoreCase(OidcBackchannelTokenDeliveryModes.PING.name())) {
            ticketRegistry.updateTicket(cibaRequest.markTicketReady());
            assertNotNull(oidcCibaAccessTokenGrantRequestExtractor.extract(context));
        }
    }
}
