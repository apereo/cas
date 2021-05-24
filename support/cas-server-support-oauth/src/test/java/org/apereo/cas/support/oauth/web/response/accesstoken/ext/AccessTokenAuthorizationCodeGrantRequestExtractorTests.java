package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.InvalidTicketException;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccessTokenAuthorizationCodeGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class AccessTokenAuthorizationCodeGrantRequestExtractorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauth20ConfigurationContext")
    private OAuth20ConfigurationContext oauth20ConfigurationContext;

    @Test
    public void verifyNoToken() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());

        val service = getRegisteredService(REDIRECT_URI, CLIENT_ID, CLIENT_SECRET);
        servicesManager.save(service);

        val response = new MockHttpServletResponse();
        val extractor = new AccessTokenAuthorizationCodeGrantRequestExtractor(oauth20ConfigurationContext);
        assertEquals(extractor.getResponseType(), OAuth20ResponseTypes.NONE);
        assertThrows(InvalidTicketException.class, () -> extractor.extract(request, response));
    }

    @Test
    public void verifyExtraction() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);

        val service = getRegisteredService(REDIRECT_URI, CLIENT_ID, CLIENT_SECRET);
        service.setGenerateRefreshToken(true);
        servicesManager.save(service);

        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();
        val extractor = new AccessTokenAuthorizationCodeGrantRequestExtractor(oauth20ConfigurationContext);
        val result = extractor.extract(request, response);
        assertNotNull(result);
    }

    @Test
    public void verifyExpiredCode() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);

        val service = getRegisteredService(REDIRECT_URI, CLIENT_ID, CLIENT_SECRET);
        service.setGenerateRefreshToken(true);
        servicesManager.save(service);

        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        code.markTicketExpired();
        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();
        val extractor = new AccessTokenAuthorizationCodeGrantRequestExtractor(oauth20ConfigurationContext);
        assertThrows(InvalidTicketException.class, () -> extractor.extract(request, response));
    }

    @Test
    public void verifyUnknownService() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, "unknown.org/abc");
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, "Unknown");

        val service = getRegisteredService(REDIRECT_URI, CLIENT_ID, CLIENT_SECRET);
        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();
        val extractor = new AccessTokenAuthorizationCodeGrantRequestExtractor(oauth20ConfigurationContext);
        assertThrows(UnauthorizedServiceException.class, () -> extractor.extract(request, response));
    }

    @Test
    public void verifyNoClientIdOrRedirectUri() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());

        val service = getRegisteredService(REDIRECT_URI, CLIENT_ID, CLIENT_SECRET);
        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();
        val extractor = new AccessTokenAuthorizationCodeGrantRequestExtractor(oauth20ConfigurationContext);
        assertThrows(UnauthorizedServiceException.class, () -> extractor.extract(request, response));
    }

}
