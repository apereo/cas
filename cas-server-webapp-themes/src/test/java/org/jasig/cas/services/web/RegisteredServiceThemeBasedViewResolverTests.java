package org.jasig.cas.services.web;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegexRegisteredService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author John Gasper
 * @since 4.1.0
 *
 */
public class RegisteredServiceThemeBasedViewResolverTests {

    private RegisteredServiceThemeBasedViewResolver registeredServiceThemeBasedViewResolver;

    private DefaultServicesManagerImpl servicesManager;

    @Before
    public void setUp() throws Exception {
        this.servicesManager = new DefaultServicesManagerImpl(new InMemoryServiceRegistryDaoImpl());
        this.servicesManager.setApplicationEventPublisher(mock(ApplicationEventPublisher.class));
        final RegexRegisteredService r = new RegexRegisteredService();
        r.setTheme("myTheme");
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId("myServiceId");
        this.servicesManager.save(r);

        final RegexRegisteredService r2 = new RegexRegisteredService();
        r2.setTheme(null);
        r2.setId(1001);
        r2.setName("Test Service 2");
        r2.setServiceId("myDefaultId");
        this.servicesManager.save(r2);

        this.registeredServiceThemeBasedViewResolver = new RegisteredServiceThemeBasedViewResolver(this.servicesManager);
        this.registeredServiceThemeBasedViewResolver.setPrefix("/WEB-INF/view/jsp");
        this.registeredServiceThemeBasedViewResolver.setSuffix(".jsp");
    }

    
    @Test
    public void verifyGetServiceWithTheme() throws Exception {
        final MockRequestContext requestContext = new MockRequestContext();
        RequestContextHolder.setRequestContext(requestContext);

        final WebApplicationService webApplicationService = new WebApplicationServiceFactory().createService("myServiceId");
        requestContext.getFlowScope().put("service", webApplicationService);

        final InternalResourceView view = (InternalResourceView) this.registeredServiceThemeBasedViewResolver.loadView("casLoginView", 
                Locale.getDefault());
        assertNotNull(view);
        
        assertEquals("/WEB-INF/view/jsp/myTheme/ui/casLoginView.jsp", view.getUrl());
    }
    

    @Test
    public void verifyNoService() throws Exception {
        final MockRequestContext requestContext = new MockRequestContext();
        RequestContextHolder.setRequestContext(requestContext);
        final InternalResourceView view = (InternalResourceView) this.registeredServiceThemeBasedViewResolver.loadView("casLoginView",
                Locale.getDefault());
        assertNull(view);
    }
    
}
