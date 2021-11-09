package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.val;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.http.url.UrlResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcCasCallbackUrlResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDC")
public class OidcCasCallbackUrlResolverTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("casCallbackUrlResolver")
    private UrlResolver casCallbackUrlResolver;

    @Test
    public void verifyOperation() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addParameter(OAuth20Constants.CLIENT_ID, UUID.randomUUID().toString());
        request.addParameter(OAuth20Constants.STATE, UUID.randomUUID().toString());
        request.addParameter(OidcConstants.UI_LOCALES, "de");
        request.addParameter(OidcConstants.MAX_AGE, "100");

        val output = casCallbackUrlResolver.compute(
            OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()),
            new JEEContext(request, response));
        assertNotNull(output);

        val uri = new URIBuilder(output);
        assertTrue(uri.getQueryParams().stream().anyMatch(p -> p.getName().equalsIgnoreCase(OAuth20Constants.CLIENT_ID)));
        assertTrue(uri.getQueryParams().stream().anyMatch(p -> p.getName().equalsIgnoreCase(OAuth20Constants.STATE)));
        assertTrue(uri.getQueryParams().stream().anyMatch(p -> p.getName().equalsIgnoreCase(OidcConstants.UI_LOCALES)));
        assertTrue(uri.getQueryParams().stream().anyMatch(p -> p.getName().equalsIgnoreCase(OidcConstants.MAX_AGE)));
    }

}
