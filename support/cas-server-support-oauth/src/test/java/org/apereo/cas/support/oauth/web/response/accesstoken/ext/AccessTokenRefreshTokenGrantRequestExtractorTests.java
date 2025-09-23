package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.OAuth20TestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.OAuth20UnauthorizedScopeRequestException;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccessTokenRefreshTokenGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OAuth")
class AccessTokenRefreshTokenGrantRequestExtractorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("accessTokenRefreshTokenGrantRequestExtractor")
    private AccessTokenGrantRequestExtractor extractor;

    @Test
    void verifyNoService() {
        val request = new MockHttpServletRequest();
        val service = getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString(), CLIENT_SECRET);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());

        val response = new MockHttpServletResponse();
        assertEquals(OAuth20ResponseTypes.NONE, extractor.getResponseType());

        val context = new JEEContext(request, response);
        assertTrue(extractor.supports(context));

        assertThrows(UnauthorizedServiceException.class, () -> extractor.extract(context));
    }

    @Test
    void verifyScopeExtraction() throws Throwable {
        val service = getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString(), CLIENT_SECRET);
        service.setScopes(Set.of("openid", "email", "profile"));
        servicesManager.save(service);

        val refreshToken = OAuth20TestUtils.getRefreshToken(service.getServiceId(), service.getClientId());
        when(refreshToken.getScopes()).thenReturn(Set.of("openid", "email"));
        when(refreshToken.getId()).thenReturn("RT-1");
        ticketRegistry.addTicket(refreshToken);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        request.addParameter(OAuth20Constants.REFRESH_TOKEN, "RT-1");

        request.addParameter(OAuth20Constants.SCOPE, "email");
        var result = extractor.extract(new JEEContext(request, new MockHttpServletResponse()));
        assertEquals(Set.of("email"), result.getScopes());

        request.setParameter(OAuth20Constants.SCOPE, StringUtils.EMPTY);
        result = extractor.extract(new JEEContext(request, new MockHttpServletResponse()));
        assertEquals(Set.of("openid", "email"), result.getScopes());

        request.setParameter(OAuth20Constants.SCOPE, "openid email");
        result = extractor.extract(new JEEContext(request, new MockHttpServletResponse()));
        assertEquals(Set.of("openid", "email"), result.getScopes());

        request.setParameter(OAuth20Constants.SCOPE, "email profile");
        assertThrows(OAuth20UnauthorizedScopeRequestException.class,
            () -> extractor.extract(new JEEContext(request, new MockHttpServletResponse())));
    }
}
