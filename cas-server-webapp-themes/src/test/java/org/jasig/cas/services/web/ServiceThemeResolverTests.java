package org.jasig.cas.services.web;

import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.util.ServicesTestUtils;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
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
 *
 * @author Scott Battaglia
 * @since 3.1
 *
 */
public class ServiceThemeResolverTests {

    private ServiceThemeResolver serviceThemeResolver;

    private DefaultServicesManagerImpl servicesManager;

    @Before
    public void setUp() throws Exception {
        this.servicesManager = new DefaultServicesManagerImpl(new InMemoryServiceRegistryDaoImpl());
        this.servicesManager.setApplicationEventPublisher(mock(ApplicationEventPublisher.class));

        this.serviceThemeResolver = new ServiceThemeResolver();
        this.serviceThemeResolver.setDefaultThemeName("test");
        this.serviceThemeResolver.setServicesManager(this.servicesManager);
        final Map<String, String> mobileBrowsers = new HashMap<>();
        mobileBrowsers.put("Mozilla", "theme");
        this.serviceThemeResolver.setMobileBrowsers(mobileBrowsers);
    }

    @Test
    public void verifyGetServiceThemeDoesNotExist() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setTheme("myTheme");
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId("myServiceId");

        this.servicesManager.save(r);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final RequestContext ctx = mock(RequestContext.class);
        final MutableAttributeMap scope = new LocalAttributeMap();
        scope.put("service", ServicesTestUtils.getService(r.getServiceId()));
        when(ctx.getFlowScope()).thenReturn(scope);
        RequestContextHolder.setRequestContext(ctx);
        request.addHeader("User-Agent", "Mozilla");
        assertEquals("test", this.serviceThemeResolver.resolveThemeName(request));
    }

    @Test
    public void verifyGetDefaultService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "myServiceId");
        request.addHeader("User-Agent", "Mozilla");
        assertEquals("test", this.serviceThemeResolver.resolveThemeName(request));
    }

    @Test
    public void verifyGetDefaultServiceWithNoServicesManager() {
        this.serviceThemeResolver.setServicesManager(null);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "myServiceId");
        request.addHeader("User-Agent", "Mozilla");
        assertEquals("test", this.serviceThemeResolver.resolveThemeName(request));
    }



}
