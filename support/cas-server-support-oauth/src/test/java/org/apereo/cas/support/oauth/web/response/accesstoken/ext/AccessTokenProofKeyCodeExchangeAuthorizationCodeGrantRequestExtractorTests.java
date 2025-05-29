package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.util.spring.DirectObjectProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier(OAuth20ConfigurationContext.BEAN_NAME)
    private OAuth20ConfigurationContext oauth20ConfigurationContext;

    @Test
    void verifyExtraction() throws Throwable {
        val service = getRegisteredService(REDIRECT_URI, UUID.randomUUID().toString(), CLIENT_SECRET);
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "code-verifier");
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CODE_CHALLENGE, "challenge");
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());

        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        request.addParameter(OAuth20Constants.CODE, code.getId());
        
        val response = new MockHttpServletResponse();
        val extractor = new AccessTokenProofKeyCodeExchangeAuthorizationCodeGrantRequestExtractor(
            new DirectObjectProvider<>(oauth20ConfigurationContext));
        assertTrue(extractor.requestMustBeAuthenticated());

        val context = new JEEContext(request, response);
        val commonProfile = new CommonProfile();
        commonProfile.setId("testuser");
        new ProfileManager(context, oauthDistributedSessionStore).save(true, commonProfile, false);
        val result = extractor.extract(context);
        assertNotNull(result);
    }
}
