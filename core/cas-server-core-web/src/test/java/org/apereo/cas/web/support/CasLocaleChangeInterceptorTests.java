package org.apereo.cas.web.support;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.core.web.LocaleProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasLocaleChangeInterceptorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Web")
public class CasLocaleChangeInterceptorTests {
    private ServicesManager servicesManager;

    private ArgumentExtractor argumentExtractor;

    @BeforeEach
    public void setup() {
        this.argumentExtractor = mock(ArgumentExtractor.class);
        this.servicesManager = mock(ServicesManager.class);
    }

    @Test
    public void verifyDefaultRequestForUrl() throws Exception {
        val request = new MockHttpServletRequest();
        request.setPreferredLocales(List.of(Locale.FRENCH));
        request.setRequestURI("/login");
        val response = new MockHttpServletResponse();
        val resolver = new SessionLocaleResolver();
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, resolver);
        val interceptor = getInterceptor(false);
        interceptor.setSupportedFlows(List.of(CasWebflowConfigurer.FLOW_ID_LOGIN));
        interceptor.preHandle(request, response, new Object());
        assertEquals(Locale.FRENCH, resolver.resolveLocale(request));
    }

    @Test
    public void verifyServiceHasLocaleAssigned() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val resolver = new SessionLocaleResolver();
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, resolver);
        val service = RegisteredServiceTestUtils.getService();
        when(argumentExtractor.extractService(any(HttpServletRequest.class))).thenReturn(service);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        registeredService.setLocale("${T(java.util.Locale).GERMAN.getLanguage()}");
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);
        getInterceptor(false).preHandle(request, response, new Object());
        assertEquals(Locale.GERMAN, resolver.resolveLocale(request));
    }

    @Test
    public void verifyRequestHeaderBeatsCasDefault() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val resolver = new SessionLocaleResolver();
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, resolver);
        getInterceptor(false).preHandle(request, response, new Object());
        assertEquals(Locale.ENGLISH, resolver.resolveLocale(request));
    }

    @Test
    public void verifyRequestParamBeatsCasDefault() throws Exception {
        val request = new MockHttpServletRequest();
        request.addParameter("locale", "it");
        val response = new MockHttpServletResponse();
        val resolver = new SessionLocaleResolver();
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, resolver);
        getInterceptor(false).preHandle(request, response, new Object());
        assertEquals(Locale.ITALIAN, resolver.resolveLocale(request));
    }

    @Test
    public void verifyForcedCasDefaultBeatsAll() throws Exception {
        val request = new MockHttpServletRequest();
        request.addParameter("locale", "it");
        val response = new MockHttpServletResponse();

        val resolver = new SessionLocaleResolver();
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, resolver);
        getInterceptor(true).preHandle(request, response, new Object());
        assertEquals(Locale.FRENCH, resolver.resolveLocale(request));
    }

    private CasLocaleChangeInterceptor getInterceptor(final boolean force) {
        val props = new LocaleProperties().setDefaultValue("fr").setForceDefaultLocale(force);
        return new CasLocaleChangeInterceptor(props, argumentExtractor, servicesManager);
    }

}
