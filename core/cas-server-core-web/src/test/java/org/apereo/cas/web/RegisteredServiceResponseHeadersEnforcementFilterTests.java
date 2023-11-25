package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.RegisteredServicesTemplatesManager;
import org.apereo.cas.services.ServicesManagerConfigurationContext;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.mgmt.DefaultServicesManager;
import org.apereo.cas.services.web.support.RegisteredServiceResponseHeadersEnforcementFilter;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.support.filters.ResponseHeadersEnforcementFilter;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceResponseHeadersEnforcementFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RegisteredService")
class RegisteredServiceResponseHeadersEnforcementFilterTests {

    private static RegisteredServiceResponseHeadersEnforcementFilter getFilterForProperty(final RegisteredServiceProperties p) {
        return getFilterForProperty(Pair.of(p, "true"));
    }

    private static RegisteredServiceResponseHeadersEnforcementFilter getFilterForProperty(final Pair<RegisteredServiceProperties, String>... properties) {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(new InMemoryServiceRegistry(appCtx))
            .applicationContext(appCtx)
            .registeredServicesTemplatesManager(mock(RegisteredServicesTemplatesManager.class))
            .environments(new HashSet<>(0))
            .servicesCache(Caffeine.newBuilder().build())
            .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
            .build();

        val servicesManager = new DefaultServicesManager(context);
        val argumentExtractor = new DefaultArgumentExtractor(new WebApplicationServiceFactory());

        val service = RegisteredServiceTestUtils.getRegisteredService("service-0", Map.of());
        val props1 = new LinkedHashMap<String, RegisteredServiceProperty>();
        for (val property : properties) {
            val prop1 = new DefaultRegisteredServiceProperty();
            prop1.addValue(property.getValue());
            props1.put(property.getKey().getPropertyName(), prop1);
        }
        service.setProperties(props1);
        servicesManager.save(service);

        return new RegisteredServiceResponseHeadersEnforcementFilter(new DirectObjectProvider<>(servicesManager),
            new DirectObjectProvider<>(argumentExtractor),
            new DirectObjectProvider<>(new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy())),
            new DirectObjectProvider<>(new RegisteredServiceAccessStrategyAuditableEnforcer(appCtx)));
    }

    @Test
    void verifyServiceUnauthorized() throws Throwable {
        val filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, UUID.randomUUID().toString());
        val servletContext = new MockServletContext();
        val filterConfig = new MockFilterConfig(servletContext);
        filter.init(filterConfig);
        assertThrows(UnauthorizedServiceException.class, () -> filter.doFilter(request, response, new MockFilterChain()));
        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    void verifyCacheControl() throws Throwable {
        val filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        val servletContext = new MockServletContext();
        val filterConfig = new MockFilterConfig(servletContext);
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_CACHE_CONTROL_STATIC_RESOURCES, "css|js|png|txt|jpg|ico|jpeg|bmp|gif");
        filter.init(filterConfig);
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("Cache-Control"));
    }

    @Test
    void verifyCacheControlDisabled() throws Throwable {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL, "false"));
        filter.setEnableCacheControl(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("Cache-Control"));
    }

    @Test
    void verifyContentSecurityPolicy() throws Throwable {
        val filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_CONTENT_SECURITY_POLICY);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        request.setRequestURI("/cas/login");
        filter.setContentSecurityPolicy("sample-policy");
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("Content-Security-Policy"));
    }

    @Test
    void verifyContentSecurityPolicyDisabled() throws Throwable {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_CONTENT_SECURITY_POLICY, "false"));
        filter.setContentSecurityPolicy(null);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        request.setRequestURI("/cas/login");
        filter.setContentSecurityPolicy("sample-policy");
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("Content-Security-Policy"));
    }

    @Test
    void verifyStrictTransport() throws Throwable {
        val filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_STRICT_TRANSPORT_SECURITY);
        filter.setStrictTransportSecurityHeader("max-age=1");
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        request.setSecure(true);
        filter.doFilter(request, response, new MockFilterChain());
        filter.doFilter(request, response, new MockFilterChain());
        val header = response.getHeader("Strict-Transport-Security");
        assertEquals("max-age=1", header);
    }

    @Test
    void verifyStrictTransportDisabled() throws Throwable {
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
    void verifyXContentOptions() throws Throwable {
        val filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("X-Content-Type-Options"));
    }

    @Test
    void verifyXContentOptionsDisabled() throws Throwable {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS, "false"));
        filter.setEnableXContentTypeOptions(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("X-Content-Type-Options"));
    }

    @Test
    void verifyOptionForUnknownService() throws Throwable {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS, "false"));
        filter.setEnableXContentTypeOptions(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "unknown-123456");
        assertThrows(UnauthorizedServiceException.class,
            () -> filter.doFilter(request, response, new MockFilterChain()));
    }

    @Test
    void verifyXframeOptions() throws Throwable {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XFRAME_OPTIONS, "true"),
            Pair.of(RegisteredServiceProperties.HTTP_HEADER_XFRAME_OPTIONS, "sameorigin"));

        filter.setXframeOptions("some-other-value");
        filter.setEnableXFrameOptions(true);

        var response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertEquals("sameorigin", response.getHeader("X-Frame-Options"));
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-something-else");
        assertThrows(UnauthorizedServiceException.class,
            () -> filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain()));
    }

    @Test
    void verifyXframeOptionsDisabled() throws Throwable {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XFRAME_OPTIONS, "false"));

        filter.setXframeOptions("some-other-value");
        filter.setEnableXFrameOptions(true);

        var response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("X-Frame-Options"));

        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-something-else");
        assertThrows(UnauthorizedServiceException.class,
            () -> filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain()));
    }

    @Test
    void verifyXssProtection() throws Throwable {
        val filter = getFilterForProperty(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XSS_PROTECTION);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("X-XSS-Protection"));
    }

    @Test
    void verifyXssProtectionDisabled() throws Throwable {
        val filter = getFilterForProperty(Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XSS_PROTECTION, "false"));
        filter.setEnableXSSProtection(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-0");
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("X-XSS-Protection"));
    }
}
