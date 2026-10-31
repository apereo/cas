package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultCasAuthenticationBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class OAuth20DefaultCasAuthenticationBuilderTests extends BaseOAuth20AuthenticatorTests {

    @Autowired
    @Qualifier("oauthCasAuthenticationBuilder")
    private OAuth20CasAuthenticationBuilder authenticationBuilder;

    @Test
    void verifyOperationByService() {
        val request = new MockHttpServletRequest();
        request.addHeader("X-".concat(CasProtocolConstants.PARAMETER_SERVICE), service.getServiceId());
        val result = authenticationBuilder.buildService(service, request, true);
        assertNotNull(result);
    }

    @Test
    void verifyOperationToBuild() throws Throwable {
        val profile = new DefaultOAuth20UserProfile("casuser", OAuth20UserProfile.COMMON_PROFILE_HANDLER_NAME);
        profile.addAuthenticationAttribute("clazz", "high");
        profile.addAttribute("cn", "casuser");

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.STATE, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.NONCE, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.ACR_VALUES, "mfa-dummy");
        request.addHeader("X-".concat(CasProtocolConstants.PARAMETER_SERVICE), service.getServiceId());
        val result = authenticationBuilder.build(profile, service,
            request, RegisteredServiceTestUtils.getService());
        assertNotNull(result);
        assertTrue(result.getPrincipal().getAttributes().containsKey("cn"));
        assertTrue(result.getAttributes().containsKey("clazz"));
        assertEquals(Set.of("OAuth20CommonProfile"), result.getSuccesses().keySet());
    }

}
