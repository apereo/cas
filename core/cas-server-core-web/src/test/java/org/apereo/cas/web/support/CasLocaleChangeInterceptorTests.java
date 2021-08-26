package org.apereo.cas.web.support;

import org.apereo.cas.configuration.model.core.web.LocaleProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasLocaleChangeInterceptorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Web")
public class CasLocaleChangeInterceptorTests {

    @Test
    public void verifyRequestHeaderBeatsCasDefault() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val resolver = new SessionLocaleResolver();
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, resolver);
        val interceptor = new CasLocaleChangeInterceptor(new LocaleProperties().setDefaultValue("fr"));
        interceptor.preHandle(request, response, new Object());
        assertEquals(Locale.ENGLISH, resolver.resolveLocale(request));
    }

    @Test
    public void verifyRequestParamBeatsCasDefault() throws Exception {
        val request = new MockHttpServletRequest();
        request.addParameter("locale", "it");
        val response = new MockHttpServletResponse();

        val resolver = new SessionLocaleResolver();
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, resolver);
        val interceptor = new CasLocaleChangeInterceptor(new LocaleProperties().setDefaultValue("fr"));
        interceptor.preHandle(request, response, new Object());
        assertEquals(Locale.ITALIAN, resolver.resolveLocale(request));
    }

    @Test
    public void verifyForcedCasDefaultBeatsAll() throws Exception {
        val request = new MockHttpServletRequest();
        request.addParameter("locale", "it");
        val response = new MockHttpServletResponse();

        val resolver = new SessionLocaleResolver();
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, resolver);
        val interceptor = new CasLocaleChangeInterceptor(new LocaleProperties().setDefaultValue("fr").setForceDefaultLocale(true));
        interceptor.preHandle(request, response, new Object());
        assertEquals(Locale.FRENCH, resolver.resolveLocale(request));
    }

}
