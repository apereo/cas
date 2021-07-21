package org.apereo.cas.oidc.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.oidc.AbstractOidcTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcLocaleChangeInterceptorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDC")
public class OidcLocaleChangeInterceptorTests extends AbstractOidcTests {

    @Autowired
    @Qualifier("oidcLocaleChangeInterceptor")
    private HandlerInterceptor oidcLocaleChangeInterceptor;
    
    @Test
    public void verifyOperation() throws Exception {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        assertFalse(oidcLocaleChangeInterceptor.preHandle(request, response, new Object()));
        val service = "https://localhost/cas/authz?ui_locales=de";
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service);
        assertTrue(oidcLocaleChangeInterceptor.preHandle(request, response, new Object()));
    }
}
