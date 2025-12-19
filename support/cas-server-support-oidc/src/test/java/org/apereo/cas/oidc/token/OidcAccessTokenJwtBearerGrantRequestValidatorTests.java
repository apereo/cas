package org.apereo.cas.oidc.token;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAccessTokenJwtBearerGrantRequestValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("OIDC")
class OidcAccessTokenJwtBearerGrantRequestValidatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcAccessTokenJwtBearerGrantTypeTokenRequestValidator")
    private OAuth20TokenRequestValidator validator;

    @Test
    void verifySubjectTokenAsIdToken() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);
        request.addParameter(OAuth20Constants.ASSERTION, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.JWT_BEARER.getType());
        assertTrue(validator.supports(context));
        assertTrue(validator.validate(context));
    }
}
