package org.apereo.cas.services.web;

import org.apereo.cas.services.DefaultServicesManager;
import org.apereo.cas.services.InMemoryServiceRegistry;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
        
/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class ServiceThemeResolverTests {

    private static final String MOZILLA = "Mozilla";
    private static final String DEFAULT_THEME_NAME = "test";
    private ServiceThemeResolver serviceThemeResolver;
    private DefaultServicesManager servicesManager;
    private Map<String, String> mobileBrowsers;

    @Before
    public void setUp() throws Exception {
        this.servicesManager = new DefaultServicesManager(new InMemoryServiceRegistry());

        mobileBrowsers = new HashMap<>();
        mobileBrowsers.put(MOZILLA, "theme");
        this.serviceThemeResolver = new ServiceThemeResolver(DEFAULT_THEME_NAME, servicesManager, mobileBrowsers);
    }

    @Test
    public void verifyGetServiceThemeDoesNotExist() {
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setTheme("myTheme");
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId("myServiceId");

        this.servicesManager.save(r);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final RequestContext ctx = mock(RequestContext.class);
        final MutableAttributeMap scope = new LocalAttributeMap();
        scope.put("service", RegisteredServiceTestUtils.getService(r.getServiceId()));
        when(ctx.getFlowScope()).thenReturn(scope);
        RequestContextHolder.setRequestContext(ctx);
        request.addHeader(WebUtils.USER_AGENT_HEADER, MOZILLA);
        assertEquals(DEFAULT_THEME_NAME, this.serviceThemeResolver.resolveThemeName(request));
    }

    @Test
    public void verifyGetDefaultService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "myServiceId");
        request.addHeader(WebUtils.USER_AGENT_HEADER, MOZILLA);
        assertEquals(DEFAULT_THEME_NAME, this.serviceThemeResolver.resolveThemeName(request));
    }

    @Test
    public void verifyGetDefaultServiceWithNoServicesManager() {
        this.serviceThemeResolver = new ServiceThemeResolver(DEFAULT_THEME_NAME, null, mobileBrowsers);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "myServiceId");
        request.addHeader(WebUtils.USER_AGENT_HEADER, MOZILLA);
        assertEquals(DEFAULT_THEME_NAME, this.serviceThemeResolver.resolveThemeName(request));
    }
}
