package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.CasProtocolConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20CasAuthenticationBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20CasAuthenticationBuilderTests extends BaseOAuth20AuthenticatorTests {

    @Autowired
    @Qualifier("oauthCasAuthenticationBuilder")
    private OAuth20CasAuthenticationBuilder authenticationBuilder;

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        request.addHeader("X-".concat(CasProtocolConstants.PARAMETER_SERVICE), service.getServiceId());
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        val result = authenticationBuilder.buildService(service, ctx, true);
        assertNotNull(result);
    }

}
