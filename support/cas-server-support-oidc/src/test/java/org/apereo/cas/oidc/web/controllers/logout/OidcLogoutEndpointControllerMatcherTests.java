package org.apereo.cas.oidc.web.controllers.logout;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases owned by {@link OidcLogoutEndpointControllerMatcherTests}.
 *
 * @author Christian Migowski
 * @since 6.4.0
 */
@Tag("OIDC")
@Import(OidcLogoutEndpointControllerMatcherTests.TestConfig.class)
public class OidcLogoutEndpointControllerMatcherTests extends AbstractOidcTests {

    @Autowired
    @Qualifier("oidcLogoutEndpointController")
    protected OidcLogoutEndpointController oidcLogoutEndpointController;

    @Test
    public void verifyOidcLogoutWithIdTokenAndValidRegExMatchingPostLogoutRedirectUrlParams() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val claims = getClaims();
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val idToken = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);

        var result = oidcLogoutEndpointController.handleRequestInternal("https://www.acme.com/end", "abcd1234", idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCodeValue());
        var redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertEquals("https://www.acme.com/end?state=abcd1234&client_id=clientid", redirectUrl);

        result = oidcLogoutEndpointController.handleRequestInternal("https://www.acme.com/done", "abcd1234", idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCodeValue());
        redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertEquals("https://www.acme.com/done?state=abcd1234&client_id=clientid", redirectUrl);

        result = oidcLogoutEndpointController.handleRequestInternal("https://www.acme.org/done", "abcd1234", idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCodeValue());
        redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
        assertEquals("https://oauth.example.org/logout?state=abcd1234&client_id=clientid", redirectUrl);
    }

    @TestConfiguration
    public static class TestConfig {
        @Bean(name = OidcPostLogoutRedirectUrlMatcher.BEAN_NAME_POST_LOGOUT_REDIRECT_URL_MATCHER)
        public OidcPostLogoutRedirectUrlMatcher postLogoutRedirectUrlMatcher() {
            return String::matches;
        }
    }

}
