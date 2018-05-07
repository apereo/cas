package org.apereo.cas.services.web.support;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceResponseHeadersEnforcementFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@RunWith(MockitoJUnitRunner.class)
public class RegisteredServiceResponseHeadersEnforcementFilterTests {

    @Test
    public void verifyCacheControl() throws Exception {
        final var filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL);
        final var response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());
        assertNotNull(response.getHeader("Cache-Control"));
    }

    @Test
    public void verifyContentSecurityPolicy() throws Exception {
        final var filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_CONTENT_SECURITY_POLICY);
        final var response = new MockHttpServletResponse();
        final var request = new MockHttpServletRequest();
        request.setRequestURI("/cas/login");
        filter.setContentSecurityPolicy("sample-policy");
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("Content-Security-Policy"));
    }

    @Test
    public void verifyStrictTransport() throws Exception {
        final var filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_STRICT_TRANSPORT_SECURITY);
        final var response = new MockHttpServletResponse();
        final var request = new MockHttpServletRequest();
        request.setSecure(true);
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("Strict-Transport-Security"));
    }

    @Test
    public void verifyXContentOptions() throws Exception {
        final var filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS);
        final var response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());
        assertNotNull(response.getHeader("X-Content-Type-Options"));
    }

    @Test
    public void verifyXframeOptions() throws Exception {
        final var filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XFRAME_OPTIONS);
        final var response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());
        assertNotNull(response.getHeader("X-Frame-Options"));
    }

    @Test
    public void verifyXssProtection() throws Exception {
        final var filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XSS_PROTECTION);
        final var response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());
        assertNotNull(response.getHeader("X-XSS-Protection"));
    }

    private RegisteredServiceResponseHeadersEnforcementFilter getFilterForProperty(final RegisteredServiceProperties p) {
        final var servicesManager = mock(ServicesManager.class);
        final var argumentExtractor = mock(ArgumentExtractor.class);
        final var webApplicationService = mock(WebApplicationService.class);
        when(argumentExtractor.extractService(any(HttpServletRequest.class))).thenReturn(webApplicationService);

        final var registeredService = mock(RegisteredService.class);
        final Map<String, RegisteredServiceProperty> props = new LinkedHashMap<>();
        final var prop = mock(RegisteredServiceProperty.class);
        when(prop.getValue()).thenReturn(Boolean.TRUE.toString());
        props.put(p.getPropertyName(), prop);
        when(registeredService.getProperties()).thenReturn(props);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);
        return new RegisteredServiceResponseHeadersEnforcementFilter(servicesManager, argumentExtractor);
    }
}
