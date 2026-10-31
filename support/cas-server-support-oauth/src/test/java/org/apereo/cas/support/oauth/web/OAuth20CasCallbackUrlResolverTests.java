package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
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
    private OAuth20CasCallbackUrlResolver casCallbackUrlResolver;

    @Test
    void verifyOperation() throws Throwable {
        val registeredService = addRegisteredService();

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.RESPONSE_MODE, OAuth20ResponseModeTypes.FORM_POST.getType());
        request.addParameter(OAuth20Constants.STATE, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.NONCE, UUID.randomUUID().toString());
        request.addParameter("c1", UUID.randomUUID().toString());
        request.addParameter("c2", UUID.randomUUID().toString());
        val output = casCallbackUrlResolver.compute(
            OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()), request);
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

    @Test
    void verifyCallbackUrlForRequest() throws Throwable {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        val output = casCallbackUrlResolver.compute(request);
        assertEquals(OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix())
            + "?client_id=client&client_name=" + Authenticators.CAS_OAUTH_CLIENT, output);
    }

    @Test
    void verifyServiceForTicket() {
        val callbackUrl = OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix());
        val request = new MockHttpServletRequest();
        assertEquals(callbackUrl, casCallbackUrlResolver.computeServiceForTicket(request));
        request.setQueryString("client_id=client&redirect_uri=https%3A%2F%2Fexample.org&client_name=CasOAuthClient&ticket=ST-1");
        assertEquals(callbackUrl + "?client_id=client&redirect_uri=https%3A%2F%2Fexample.org&client_name=CasOAuthClient",
            casCallbackUrlResolver.computeServiceForTicket(request));
        request.setQueryString("ticket=ST-1");
        assertEquals(callbackUrl, casCallbackUrlResolver.computeServiceForTicket(request));
    }
}
