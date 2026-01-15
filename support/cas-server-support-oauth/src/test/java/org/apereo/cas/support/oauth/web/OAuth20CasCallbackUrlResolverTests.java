package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.http.url.UrlResolver;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20CasCallbackUrlResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuthWeb")
class OAuth20CasCallbackUrlResolverTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("casCallbackUrlResolver")
    private UrlResolver casCallbackUrlResolver;

    @Test
    void verifyOperation() throws Throwable {
        val registeredService = addRegisteredService();

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.RESPONSE_MODE, OAuth20ResponseModeTypes.FORM_POST.getType());
        request.addParameter(OAuth20Constants.STATE, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.NONCE, UUID.randomUUID().toString());
        request.addParameter("c1", UUID.randomUUID().toString());
        request.addParameter("c2", UUID.randomUUID().toString());
        val output = casCallbackUrlResolver.compute(
            OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()),
            new JEEContext(request, response));
        assertNotNull(output);

        val uri = new URIBuilder(output);
        val queryParams = uri.getQueryParams();
        assertTrue(queryParams.stream().anyMatch(p -> p.getName().equalsIgnoreCase(OAuth20Constants.STATE)));
        assertTrue(queryParams.stream().anyMatch(p -> p.getName().equalsIgnoreCase(OAuth20Constants.RESPONSE_MODE)));
        assertTrue(queryParams.stream().anyMatch(p -> p.getName().equalsIgnoreCase(OAuth20Constants.CLIENT_ID)));
        assertTrue(queryParams.stream().anyMatch(p -> p.getName().equalsIgnoreCase(OAuth20Constants.NONCE)));
        assertTrue(queryParams.stream().anyMatch(p -> "c1".equalsIgnoreCase(p.getName())));
        assertTrue(queryParams.stream().anyMatch(p -> "c2".equalsIgnoreCase(p.getName())));
    }

}
