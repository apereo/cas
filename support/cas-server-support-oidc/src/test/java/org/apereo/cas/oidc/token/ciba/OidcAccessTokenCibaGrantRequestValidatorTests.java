package org.apereo.cas.oidc.token.ciba;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAccessTokenCibaGrantRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDC")
class OidcAccessTokenCibaGrantRequestValidatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcAccessTokenCibaGrantRequestValidator")
    private OAuth20TokenRequestValidator oidcAccessTokenCibaGrantRequestValidator;
    
    @ParameterizedTest
    @EnumSource(OidcBackchannelTokenDeliveryModes.class)
    void verifyOperation(final OidcBackchannelTokenDeliveryModes mode) throws Throwable {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        registeredService.setBackchannelTokenDeliveryMode(mode.getMode());
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
        assertTrue(oidcAccessTokenCibaGrantRequestValidator.supports(context));

        val profile = new CommonProfile();
        profile.setClientName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);
        profile.setId(registeredService.getClientId());
        profile.addAttribute(OAuth20Constants.CLIENT_ID, registeredService.getClientId());

        val profileManager = new ProfileManager(context, oauthDistributedSessionStore);
        profileManager.save(true, profile, false);

        assertFalse(oidcAccessTokenCibaGrantRequestValidator.validate(context));
        ticketRegistry.updateTicket(cibaRequest.markTicketReady());
        assertEquals(mode != OidcBackchannelTokenDeliveryModes.PUSH, oidcAccessTokenCibaGrantRequestValidator.validate(context));
    }
}
