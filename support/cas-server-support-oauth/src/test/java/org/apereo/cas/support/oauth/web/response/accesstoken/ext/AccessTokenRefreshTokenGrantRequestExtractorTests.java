package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccessTokenRefreshTokenGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OAuth")
public class AccessTokenRefreshTokenGrantRequestExtractorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauth20ConfigurationContext")
    private OAuth20ConfigurationContext oauth20ConfigurationContext;

    @Test
    public void verifyNoService() {
        val request = new MockHttpServletRequest();
        val service = getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString(), CLIENT_SECRET);
        request.addParameter(OAuth20Constants.REDIRECT_URI, service.getRedirectUrl());
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        
        val response = new MockHttpServletResponse();
        val extractor = new AccessTokenRefreshTokenGrantRequestExtractor(oauth20ConfigurationContext);
        assertEquals(OAuth20ResponseTypes.NONE, extractor.getResponseType());
        assertTrue(extractor.supports(request));

        assertThrows(UnauthorizedServiceException.class, () -> extractor.extract(request, response));
    }
}
