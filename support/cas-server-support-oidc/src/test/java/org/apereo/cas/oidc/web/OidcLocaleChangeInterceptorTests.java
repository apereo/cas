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
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

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
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, new SessionLocaleResolver());

        oidcLocaleChangeInterceptor.preHandle(request, response, new Object());
        assertNull(request.getAttribute(Locale.class.getName()));

        val service = "https://localhost/cas/authz?ui_locales=de";
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service);
        oidcLocaleChangeInterceptor.preHandle(request, response, new Object());
        assertNotNull(request.getAttribute(Locale.class.getName()));
    }
}
