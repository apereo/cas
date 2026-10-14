package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.view.RedirectView;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAuthorizationModelAndViewBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.oidc.discovery.authorization-response-issuer-parameter-supported=true")
class OidcAuthorizationModelAndViewBuilderTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oauthAuthorizationModelAndViewBuilder")
    private OAuth20AuthorizationModelAndViewBuilder oauthAuthorizationModelAndViewBuilder;

    @Test
    void verifyOperationForOidc() throws Throwable {
        val parameters = new HashMap<String, String>();
        val registeredService = getOidcRegisteredService();
        val results = oauthAuthorizationModelAndViewBuilder.build(registeredService,
            OAuth20ResponseModeTypes.FORM_POST, "https://localhost:8443/app/redirect", parameters);
        assertTrue(results.getModel().containsKey("originalUrl"));
        val url = results.getModel().get("originalUrl").toString();
        assertFalse(url.contains(OidcConstants.ISS.concat("=")));
        val params = (Map) results.getModel().get("parameters");
        assertTrue(params.containsKey(OidcConstants.ISS));
    }

    @Test
    void verifyOperationForNonOidc() throws Throwable {
        val parameters = new HashMap<String, String>();
        val registeredService = getOAuthRegisteredService(UUID.randomUUID().toString(), "https://localhost:8443/app/redirect");
        val results = oauthAuthorizationModelAndViewBuilder.build(registeredService,
            OAuth20ResponseModeTypes.NONE, "https://localhost:8443/app/redirect", parameters);
        val view = (RedirectView) results.getView();
        assertEquals("https://localhost:8443/app/redirect", Objects.requireNonNull(view).getUrl());
    }

    @Test
    void verifyIssuerOnlyInFragmentForFragmentResponseMode() throws Throwable {
        val parameters = new HashMap<String, String>();
        parameters.put("access_token", "AT-123");
        val registeredService = getOidcRegisteredService();
        val results = oauthAuthorizationModelAndViewBuilder.build(registeredService,
            OAuth20ResponseModeTypes.FRAGMENT, "https://localhost:8443/app/redirect?x=1", parameters);
        val url = new URIBuilder(Objects.requireNonNull((RedirectView) results.getView()).getUrl());
        assertEquals(List.of("x=1"), url.getQueryParams().stream().map(Object::toString).toList());
        assertTrue(url.getFragment().contains(OidcConstants.ISS.concat("=")));
    }

    @Test
    void verifyRedirectQueryNotRepeatedForFragmentResponseMode() throws Throwable {
        val parameters = new HashMap<String, String>();
        parameters.put("access_token", "AT-123");
        val registeredService = getOAuthRegisteredService(UUID.randomUUID().toString(), "https://localhost:8443/app/redirect");
        val results = oauthAuthorizationModelAndViewBuilder.build(registeredService,
            OAuth20ResponseModeTypes.FRAGMENT, "https://localhost:8443/app/redirect?x=1&y=2", parameters);
        val url = new URIBuilder(Objects.requireNonNull((RedirectView) results.getView()).getUrl());
        assertEquals(List.of("x=1", "y=2"), url.getQueryParams().stream().map(Object::toString).toList());
        assertEquals("access_token=AT-123", url.getFragment());
    }

    @Test
    void verifyIssuerSentOnceForQueryResponseMode() throws Throwable {
        val parameters = new HashMap<String, String>();
        parameters.put("code", "OC-123");
        val registeredService = getOidcRegisteredService();
        val results = oauthAuthorizationModelAndViewBuilder.build(registeredService,
            OAuth20ResponseModeTypes.QUERY, "https://localhost:8443/app/redirect", parameters);
        val response = new MockHttpServletResponse();
        Objects.requireNonNull(results.getView()).render(results.getModel(), new MockHttpServletRequest(), response);
        val url = new URIBuilder(Objects.requireNonNull(response.getRedirectedUrl()));
        assertEquals(1, url.getQueryParams().stream().filter(param -> OidcConstants.ISS.equals(param.getName())).count());
        assertEquals(1, url.getQueryParams().stream().filter(param -> "code".equals(param.getName())).count());
    }
}
