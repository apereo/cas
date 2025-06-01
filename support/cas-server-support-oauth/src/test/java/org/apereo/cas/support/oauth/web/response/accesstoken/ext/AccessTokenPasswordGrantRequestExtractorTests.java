package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.util.spring.DirectObjectProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccessTokenPasswordGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class AccessTokenPasswordGrantRequestExtractorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier(OAuth20ConfigurationContext.BEAN_NAME)
    private OAuth20ConfigurationContext oauth20ConfigurationContext;

    @Test
    void verifyNoProfile() throws Throwable {
        val service = getRegisteredService(REDIRECT_URI, UUID.randomUUID().toString(), CLIENT_SECRET);
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());

        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();
        val extractor = new AccessTokenPasswordGrantRequestExtractor(new DirectObjectProvider<>(oauth20ConfigurationContext));
        assertTrue(extractor.requestMustBeAuthenticated());
        assertNull(extractor.getResponseType());

        val context = new JEEContext(request, response);
        assertThrows(UnauthorizedServiceException.class, () -> extractor.extract(context));
    }

}
