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
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.services.web.support.RegisteredServiceResponseHeadersEnforcementFilter;
import org.apereo.cas.web.support.DefaultArgumentExtractor;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashSet;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceResponseHeadersEnforcementFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RegisteredService")
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
    public void verifyCacheControlDisabled() throws Exception {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL, "false"));  
        filter.setEnableCacheControl(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("Cache-Control"));
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
    public void verifyStrictTransportDisabled() throws Exception {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_STRICT_TRANSPORT_SECURITY, "false"));
        filter.setEnableStrictTransportSecurity(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        request.setSecure(true);
        filter.doFilter(request, response, new MockFilterChain());
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("Strict-Transport-Security"));
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
    public void verifyXContentOptionsDisabled() throws Exception {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS, "false"));
        filter.setEnableXContentTypeOptions(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("X-Content-Type-Options"));
    }

    @Test
    public void verifyXframeOptions() throws Exception {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XFRAME_OPTIONS, "true"),
            Pair.of(RegisteredServiceProperties.HTTP_HEADER_XFRAME_OPTIONS, "sameorigin"));

        filter.setXframeOptions("some-other-value");
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
    public void verifyXframeOptionsDisabled() throws Exception {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XFRAME_OPTIONS, "false"));

        filter.setXframeOptions("some-other-value");
        filter.setEnableXFrameOptions(true);

        var response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("X-Frame-Options"));

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

    @Test
    public void verifyXssProtectionDisabled() throws Exception {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XSS_PROTECTION, "false"));
        filter.setEnableXSSProtection(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("X-XSS-Protection"));
    }

    private static RegisteredServiceResponseHeadersEnforcementFilter getFilterForProperty(final RegisteredServiceProperties p) {
        return getFilterForProperty(Pair.of(p, "true"));
    }

    private static RegisteredServiceResponseHeadersEnforcementFilter getFilterForProperty(final Pair<RegisteredServiceProperties, String>... properties) {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(new InMemoryServiceRegistry(appCtx))
            .applicationContext(appCtx)
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .build();

        val servicesManager = new DefaultServicesManager(context);
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
