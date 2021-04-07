package org.apereo.cas.oidc.web.controllers.logout;

import lombok.val;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.config.OidcConfiguration;
import org.apereo.cas.web.support.WebUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("OIDC")
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

        ResponseEntity<HttpStatus> result = oidcLogoutEndpointController.handleRequestInternal("https://www.acme.com/end", "abcd1234", idToken, request, response);
        assertEquals(HttpStatus.PERMANENT_REDIRECT.value(), result.getStatusCodeValue());
        String redirectUrl = WebUtils.getLogoutRedirectUrl(request, String.class);
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
}
