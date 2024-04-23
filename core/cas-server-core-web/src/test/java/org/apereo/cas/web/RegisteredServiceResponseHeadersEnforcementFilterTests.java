package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.web.support.RegisteredServiceResponseHeadersEnforcementFilter;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.support.filters.ResponseHeadersEnforcementFilter;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceResponseHeadersEnforcementFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreAutoConfiguration.class
})
class RegisteredServiceResponseHeadersEnforcementFilterTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private RegisteredServiceResponseHeadersEnforcementFilter getFilterForProperty(final String serviceId,
                                                                                   final RegisteredServiceProperties property) {
        return getFilterForProperty(serviceId, Pair.of(property, "true"));
    }

    private RegisteredServiceResponseHeadersEnforcementFilter getFilterForProperty(
        final String serviceId,
        final Pair<RegisteredServiceProperties, String>... properties) {
        val argumentExtractor = new DefaultArgumentExtractor(new WebApplicationServiceFactory());

        val service = RegisteredServiceTestUtils.getRegisteredService(serviceId, Map.of());
        val props1 = new LinkedHashMap<String, RegisteredServiceProperty>();
        for (val property : properties) {
            val prop1 = new DefaultRegisteredServiceProperty();
            prop1.addValue(property.getValue());
            props1.put(property.getKey().getPropertyName(), prop1);
        }
        service.setProperties(props1);
        servicesManager.save(service);

        return new RegisteredServiceResponseHeadersEnforcementFilter(
            new DirectObjectProvider<>(servicesManager),
            new DirectObjectProvider<>(argumentExtractor),
            new DirectObjectProvider<>(new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy())),
            new DirectObjectProvider<>(new RegisteredServiceAccessStrategyAuditableEnforcer(applicationContext)));
    }

    @Test
    void verifyServiceUnauthorized() throws Throwable {
        val filter = getFilterForProperty(UUID.randomUUID().toString(), RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL);
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
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        val servletContext = new MockServletContext();
        val filterConfig = new MockFilterConfig(servletContext);
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_CACHE_CONTROL_STATIC_RESOURCES, "css|js|png|txt|jpg|ico|jpeg|bmp|gif");
        filter.init(filterConfig);
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("Cache-Control"));
    }

    @Test
    void verifyCacheControlDisabled() throws Throwable {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL, "false"));
        filter.setEnableCacheControl(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("Cache-Control"));
    }

    @Test
    void verifyContentSecurityPolicy() throws Throwable {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, RegisteredServiceProperties.HTTP_HEADER_ENABLE_CONTENT_SECURITY_POLICY);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        request.setRequestURI("/cas/login");
        filter.setContentSecurityPolicy("sample-policy");
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("Content-Security-Policy"));
    }

    @Test
    void verifyContentSecurityPolicyDisabled() throws Throwable {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_CONTENT_SECURITY_POLICY, "false"));
        filter.setContentSecurityPolicy(null);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        request.setRequestURI("/cas/login");
        filter.setContentSecurityPolicy("sample-policy");
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("Content-Security-Policy"));
    }

    @Test
    void verifyStrictTransport() throws Throwable {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, RegisteredServiceProperties.HTTP_HEADER_ENABLE_STRICT_TRANSPORT_SECURITY);
        filter.setStrictTransportSecurityHeader("max-age=1");
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        request.setSecure(true);
        filter.doFilter(request, response, new MockFilterChain());
        val header = response.getHeader("Strict-Transport-Security");
        assertEquals("max-age=1", header);
    }

    @Test
    void verifyStrictTransportDisabled() throws Throwable {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_STRICT_TRANSPORT_SECURITY, "false"));
        filter.setEnableStrictTransportSecurity(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        request.setSecure(true);
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("Strict-Transport-Security"));
    }

    @Test
    void verifyXContentOptions() throws Throwable {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("X-Content-Type-Options"));
    }

    @Test
    void verifyXContentOptionsDisabled() throws Throwable {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS, "false"));
        filter.setEnableXContentTypeOptions(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("X-Content-Type-Options"));
    }

    @Test
    void verifyOptionForUnknownService() throws Throwable {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS, "false"));
        filter.setEnableXContentTypeOptions(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "unknown-123456");
        assertThrows(UnauthorizedServiceException.class,
            () -> filter.doFilter(request, response, new MockFilterChain()));
    }

    @Test
    void verifyXframeOptions() throws Throwable {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XFRAME_OPTIONS, "true"),
            Pair.of(RegisteredServiceProperties.HTTP_HEADER_XFRAME_OPTIONS, "sameorigin"));

        filter.setXframeOptions("some-other-value");
        filter.setEnableXFrameOptions(true);

        var response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        filter.doFilter(request, response, new MockFilterChain());
        assertEquals("sameorigin", response.getHeader("X-Frame-Options"));
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-something-else");
        assertThrows(UnauthorizedServiceException.class,
            () -> filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain()));
    }

    @Test
    void verifyXframeOptionsDisabled() throws Throwable {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XFRAME_OPTIONS, "false"));

        filter.setXframeOptions("some-other-value");
        filter.setEnableXFrameOptions(true);

        var response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("X-Frame-Options"));

        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "service-something-else");
        assertThrows(UnauthorizedServiceException.class,
            () -> filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain()));
    }

    @Test
    void verifyXssProtection() throws Throwable {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, RegisteredServiceProperties.HTTP_HEADER_ENABLE_XSS_PROTECTION);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("X-XSS-Protection"));
    }

    @Test
    void verifyXssProtectionDisabled() throws Throwable {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XSS_PROTECTION, "false"));
        filter.setEnableXSSProtection(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        filter.doFilter(request, response, new MockFilterChain());
        assertNull(response.getHeader("X-XSS-Protection"));
    }
}
