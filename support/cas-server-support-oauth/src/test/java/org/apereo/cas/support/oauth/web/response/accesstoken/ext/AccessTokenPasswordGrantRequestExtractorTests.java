package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
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
 * This is {@link AccessTokenPasswordGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class AccessTokenPasswordGrantRequestExtractorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauth20ConfigurationContext")
    private OAuth20ConfigurationContext oauth20ConfigurationContext;

    @Test
    public void verifyNoProfile() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);

        val service = getRegisteredService(REDIRECT_URI, CLIENT_ID, CLIENT_SECRET);
        servicesManager.save(service);

        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();
        val extractor = new AccessTokenPasswordGrantRequestExtractor(oauth20ConfigurationContext);
        assertTrue(extractor.requestMustBeAuthenticated());
        assertNull(extractor.getResponseType());
        assertThrows(UnauthorizedServiceException.class, () -> extractor.extract(request, response));
    }

}
