package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
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
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        val result = authenticationBuilder.buildService(service, ctx, true);
        assertNotNull(result);
    }

    @Test
    void verifyOperationToBuild() throws Throwable {
        val profile = new CasProfile();
        profile.setId("casuser");
        profile.addAuthenticationAttribute("clazz", "high");
        profile.addAttribute("cn", "casuser");

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.STATE, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.NONCE, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.ACR_VALUES, "mfa-dummy");
        request.addHeader("X-".concat(CasProtocolConstants.PARAMETER_SERVICE), service.getServiceId());
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        val result = authenticationBuilder.build(profile, service,
            ctx, RegisteredServiceTestUtils.getService());
        assertNotNull(result);
        assertTrue(result.getPrincipal().getAttributes().containsKey("cn"));
        assertTrue(result.getAttributes().containsKey("clazz"));
    }

}
