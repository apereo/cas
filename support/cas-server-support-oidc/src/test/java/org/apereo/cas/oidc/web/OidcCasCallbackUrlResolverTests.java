package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
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
 * This is {@link OidcCasCallbackUrlResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDCWeb")
class OidcCasCallbackUrlResolverTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("casCallbackUrlResolver")
    private UrlResolver casCallbackUrlResolver;

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addParameter(OAuth20Constants.CLIENT_ID, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.STATE, UUID.randomUUID().toString());
        request.addParameter(OidcConstants.UI_LOCALES, "de");
        request.addParameter(OidcConstants.MAX_AGE, "100");
        request.addParameter(OidcConstants.REQUEST_URI, UUID.randomUUID().toString());

        val output = casCallbackUrlResolver.compute(
            OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()),
            new JEEContext(request, response));
        assertNotNull(output);

        val uri = new URIBuilder(output);
        assertTrue(uri.getQueryParams().stream().anyMatch(p -> p.getName().equalsIgnoreCase(OAuth20Constants.CLIENT_ID)));
        assertTrue(uri.getQueryParams().stream().anyMatch(p -> p.getName().equalsIgnoreCase(OAuth20Constants.STATE)));
        assertTrue(uri.getQueryParams().stream().anyMatch(p -> p.getName().equalsIgnoreCase(OidcConstants.UI_LOCALES)));
        assertTrue(uri.getQueryParams().stream().anyMatch(p -> p.getName().equalsIgnoreCase(OidcConstants.MAX_AGE)));
        assertTrue(uri.getQueryParams().stream().anyMatch(p -> p.getName().equalsIgnoreCase(OidcConstants.REQUEST_URI)));
    }

}
