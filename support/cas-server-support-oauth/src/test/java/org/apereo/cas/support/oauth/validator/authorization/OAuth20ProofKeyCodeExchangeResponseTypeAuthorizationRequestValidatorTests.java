package org.apereo.cas.support.oauth.validator.authorization;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;

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
 * This is {@link OAuth20ProofKeyCodeExchangeResponseTypeAuthorizationRequestValidatorTests}.
 *
 * @author Julien Huon
 * @since 6.4.0
 */
@Tag("OAuth")
public class OAuth20ProofKeyCodeExchangeResponseTypeAuthorizationRequestValidatorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauthProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator")
    private OAuth20AuthorizationRequestValidator validator;

    @Test
    public void verifySupports() throws Exception {
        val service = getRegisteredService("client", "secret");
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        assertFalse(validator.supports(context));

        request.setParameter(OAuth20Constants.CLIENT_ID, "client");
        request.setParameter(OAuth20Constants.REDIRECT_URI, service.getServiceId());
        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.TOKEN.getType());
        request.setParameter(OAuth20Constants.CODE_VERIFIER, "abcd");
        assertFalse(validator.supports(context));

        request.setParameter(OAuth20Constants.RESPONSE_TYPE, OAuth20ResponseTypes.CODE.getType());
        assertTrue(validator.supports(context));
    }
}
