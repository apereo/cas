package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.RegisteredServiceResponseHeadersEnforcementFilter;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.filters.ResponseHeadersEnforcementFilter;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.test.context.SpringBootTest;
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
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
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
    @Qualifier(ArgumentExtractor.BEAN_NAME)
    private ArgumentExtractor argumentExtractor;

    @Autowired
    private WebEndpointProperties webEndpointProperties;
    
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
    private RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;

    private RegisteredServiceResponseHeadersEnforcementFilter getFilterForProperty(final String serviceId,
                                                                                   final RegisteredServiceProperties property) {
        return getFilterForProperty(serviceId, Pair.of(property, "true"));
    }

    private RegisteredServiceResponseHeadersEnforcementFilter getFilterForProperty(
        final String serviceId,
        final Pair<RegisteredServiceProperties, String>... properties) {

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
            new DirectObjectProvider<>(new RegisteredServiceAccessStrategyAuditableEnforcer(applicationContext, principalAccessStrategyEnforcer)),
            webEndpointProperties);
    }

    @Test
    void verifyActuatorPathIgnored() {
        val filter = getFilterForProperty(UUID.randomUUID().toString(), RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setRequestURI(webEndpointProperties.getBasePath());
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, UUID.randomUUID().toString());
        val servletContext = new MockServletContext();
        val filterConfig = new MockFilterConfig(servletContext);
        filter.init(filterConfig);
        assertDoesNotThrow(() -> filter.doFilter(request, response, new MockFilterChain()));
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test
    void verifyServiceUnauthorized() {
        val filter = getFilterForProperty(UUID.randomUUID().toString(), RegisteredServiceProperties.HTTP_HEADER_ENABLE_CACHE_CONTROL);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, UUID.randomUUID().toString());
        val servletContext = new MockServletContext();
        val filterConfig = new MockFilterConfig(servletContext);
        filter.init(filterConfig);
        assertDoesNotThrow(() -> filter.doFilter(request, response, new MockFilterChain()));
        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    void verifyCacheControl() {
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
    void verifyCacheControlDisabled() {
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
    void verifyContentSecurityPolicy() {
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
    void verifyContentSecurityPolicyDisabled() {
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
    void verifyStrictTransport() {
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
    void verifyStrictTransportDisabled() {
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
    void verifyXContentOptions() {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("X-Content-Type-Options"));
    }

    @Test
    void verifyXContentOptionsDisabled() {
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
    void verifyOptionForUnknownService() {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, Pair.of(RegisteredServiceProperties.HTTP_HEADER_ENABLE_XCONTENT_OPTIONS, "false"));
        filter.setEnableXContentTypeOptions(true);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "unknown-123456");
        assertDoesNotThrow(() -> filter.doFilter(request, response, new MockFilterChain()));
        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    void verifyXframeOptions() {
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
        assertDoesNotThrow(() -> filter.doFilter(request, response, new MockFilterChain()));
        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    void verifyXframeOptionsDisabled() {
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
        assertDoesNotThrow(() -> filter.doFilter(request, response, new MockFilterChain()));
        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    void verifyXssProtection() {
        val id = UUID.randomUUID().toString();
        val filter = getFilterForProperty(id, RegisteredServiceProperties.HTTP_HEADER_ENABLE_XSS_PROTECTION);
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        filter.doFilter(request, response, new MockFilterChain());
        assertNotNull(response.getHeader("X-XSS-Protection"));
    }

    @Test
    void verifyXssProtectionDisabled() {
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
