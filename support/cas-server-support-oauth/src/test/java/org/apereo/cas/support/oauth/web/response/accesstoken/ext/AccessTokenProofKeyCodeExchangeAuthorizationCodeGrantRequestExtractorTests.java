package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauth20ConfigurationContext")
    private OAuth20ConfigurationContext oauth20ConfigurationContext;

    @Test
    public void verifyExtraction() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "code-verifier");
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CODE_CHALLENGE, "challenge");
        request.addParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);

        val service = getRegisteredService(REDIRECT_URI, CLIENT_ID, CLIENT_SECRET);
        servicesManager.save(service);

        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        request.addParameter(OAuth20Constants.CODE, code.getId());
        
        val response = new MockHttpServletResponse();
        val extractor = new AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractor(oauth20ConfigurationContext);
        assertTrue(extractor.requestMustBeAuthenticated());
        val result = extractor.extract(request, response);
        assertNotNull(result);
    }
}
