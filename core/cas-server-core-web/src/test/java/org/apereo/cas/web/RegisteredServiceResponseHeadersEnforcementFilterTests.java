package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.web.support.RegisteredServiceResponseHeadersEnforcementFilter;
import org.apereo.cas.web.support.DefaultArgumentExtractor;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceResponseHeadersEnforcementFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class RegisteredServiceResponseHeadersEnforcementFilterTests {

    @Test
    public void verifyCacheControl() throws Exception {
        val filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("Cache-Control"));
    }

    @Test
    public void verifyContentSecurityPolicy() throws Exception {
        val filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_CONTENT_SECURITY_POLICY);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setRequestURI("/cas/login");
        filter.setContentSecurityPolicy("sample-policy");
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("Content-Security-Policy"));
    }

    @Test
    public void verifyStrictTransport() throws Exception {
        val filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_STRICT_TRANSPORT_SECURITY);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        request.setSecure(true);
        filter.doFilter(request, response, new MockFilterChain());
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("Strict-Transport-Security"));
    }

    @Test
    public void verifyXContentOptions() throws Exception {
        val filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("X-Content-Type-Options"));
    }

    @Test
    public void verifyXframeOptions() throws Exception {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XFRAME_OPTIONS, "true"),
            Pair.of(RegisteredServiceProperties.HTTP_HEADER_XFRAME_OPTIONS, "sameorigin"));

        filter.setXFrameOptions("some-other-value");
        filter.setEnableXFrameOptions(true);

        var response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertEquals("sameorigin", response.getHeader("X-Frame-Options"));

        response = new MockHttpServletResponse();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-something-else");
        filter.doFilter(request, response, new MockFilterChain());
        assertEquals("some-other-value", response.getHeader("X-Frame-Options"));
    }

    @Test
    public void verifyXssProtection() throws Exception {
        val filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XSS_PROTECTION);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("X-XSS-Protection"));
    }

    private static RegisteredServiceResponseHeadersEnforcementFilter getFilterForProperty(final RegisteredServiceProperties p) {
        return getFilterForProperty(Pair.of(p, "true"));
    }

    private static RegisteredServiceResponseHeadersEnforcementFilter getFilterForProperty(final Pair<RegisteredServiceProperties, String>... properties) {
        val servicesManager = new DefaultServicesManager(new InMemoryServiceRegistry(mock(ApplicationEventPublisher.class)),
            mock(ApplicationEventPublisher.class), new LinkedHashSet<>());
        val argumentExtractor = new DefaultArgumentExtractor(new WebApplicationServiceFactory());

        val service = RegisteredServiceTestUtils.getRegisteredService("service-0");
        val props1 = new LinkedHashMap<String, RegisteredServiceProperty>();
        for (val p : properties) {
            val prop1 = new DefaultRegisteredServiceProperty();
            prop1.addValue(p.getValue());
            props1.put(p.getKey().getPropertyName(), prop1);
        }
        service.setProperties(props1);
        servicesManager.save(service);

        return new RegisteredServiceResponseHeadersEnforcementFilter(servicesManager, argumentExtractor,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));
    }
}
