package org.apereo.cas.services.web;

import org.apereo.cas.services.DefaultServicesManagerImpl;
import org.apereo.cas.services.InMemoryServiceRegistryDaoImpl;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.TestUtils;
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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
        
/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class ServiceThemeResolverTests {

    private ServiceThemeResolver serviceThemeResolver;

    private DefaultServicesManagerImpl servicesManager;

    @Before
    public void setUp() throws Exception {
        this.servicesManager = new DefaultServicesManagerImpl(new InMemoryServiceRegistryDaoImpl());

        this.serviceThemeResolver = new ServiceThemeResolver();
        this.serviceThemeResolver.setDefaultThemeName("test");
        this.serviceThemeResolver.setServicesManager(this.servicesManager);
        final Map<String, String> mobileBrowsers = new HashMap<>();
        mobileBrowsers.put("Mozilla", "theme");
        this.serviceThemeResolver.setMobileBrowsers(mobileBrowsers);
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
        scope.put("service", TestUtils.getService(r.getServiceId()));
        when(ctx.getFlowScope()).thenReturn(scope);
        RequestContextHolder.setRequestContext(ctx);
        request.addHeader(WebUtils.USER_AGENT_HEADER, "Mozilla");
        assertEquals("test", this.serviceThemeResolver.resolveThemeName(request));
    }

    @Test
    public void verifyGetDefaultService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "myServiceId");
        request.addHeader(WebUtils.USER_AGENT_HEADER, "Mozilla");
        assertEquals("test", this.serviceThemeResolver.resolveThemeName(request));
    }

    @Test
    public void verifyGetDefaultServiceWithNoServicesManager() {
        this.serviceThemeResolver.setServicesManager(null);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "myServiceId");
        request.addHeader(WebUtils.USER_AGENT_HEADER, "Mozilla");
        assertEquals("test", this.serviceThemeResolver.resolveThemeName(request));
    }
}
