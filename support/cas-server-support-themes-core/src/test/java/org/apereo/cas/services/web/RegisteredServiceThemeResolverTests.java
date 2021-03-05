package org.apereo.cas.services.web;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.io.File;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.OK;

/**
 * This is {@link RegisteredServiceThemeResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Web")
public class RegisteredServiceThemeResolverTests {
    @Test
    public void verifyNoAccess() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "Mozilla");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val strategy = new DefaultRegisteredServiceAccessStrategy(false, true);
        registeredService.setAccessStrategy(strategy);

        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);
        val plan = mock(AuthenticationServiceSelectionPlan.class);
        val service = RegisteredServiceTestUtils.getService();
        WebUtils.putServiceIntoFlowScope(context, service);
        when(plan.resolveService(any(Service.class))).thenReturn(service);
        val resolver = new RegisteredServiceThemeResolver(servicesManager, plan,
            new CasConfigurationProperties(), Map.of(RegexUtils.createPattern("Mozilla"), "Firefox"));
        resolver.setDefaultThemeName("example");
        assertEquals("example", resolver.resolveThemeName(request));
        assertNotNull(request.getAttribute("isMobile"));
        assertNotNull(request.getAttribute("browserType"));

    }

    @Test
    public void verifyNoTheme() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        registeredService.setTheme(null);
        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);
        val plan = mock(AuthenticationServiceSelectionPlan.class);
        val service = RegisteredServiceTestUtils.getService();
        WebUtils.putServiceIntoFlowScope(context, service);
        when(plan.resolveService(any(Service.class))).thenReturn(service);
        val resolver = new RegisteredServiceThemeResolver(servicesManager, plan, new CasConfigurationProperties(), Map.of());
        resolver.setDefaultThemeName("example");
        assertEquals("example", resolver.resolveThemeName(request));
    }

    @Test
    public void verifyGroovyTheme() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();

        val file = File.createTempFile("Theme", ".groovy");
        val script = IOUtils.toString(new ClassPathResource("GroovyTheme.groovy").getInputStream(), UTF_8);
        FileUtils.writeStringToFile(file, script, UTF_8);
        registeredService.setTheme(file.getCanonicalPath());

        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);
        val plan = mock(AuthenticationServiceSelectionPlan.class);
        val service = RegisteredServiceTestUtils.getService();
        WebUtils.putServiceIntoFlowScope(context, service);
        when(plan.resolveService(any(Service.class))).thenReturn(service);
        val resolver = new RegisteredServiceThemeResolver(servicesManager, plan, new CasConfigurationProperties(), Map.of());
        resolver.setDefaultThemeName("example");
        assertEquals("some-theme", resolver.resolveThemeName(request));
    }

    @Test
    public void verifyUrlTheme() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        registeredService.setTheme("http://localhost:6315");
        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);
        val plan = mock(AuthenticationServiceSelectionPlan.class);
        val service = RegisteredServiceTestUtils.getService();
        WebUtils.putServiceIntoFlowScope(context, service);
        when(plan.resolveService(any(Service.class))).thenReturn(service);
        val resolver = new RegisteredServiceThemeResolver(servicesManager, plan, new CasConfigurationProperties(), Map.of());
        resolver.setDefaultThemeName("example");

        try (val webServer = new MockWebServer(6315,
            new ByteArrayResource("custom-theme".getBytes(UTF_8), "Output"), OK)) {
            webServer.start();
            assertDoesNotThrow(new Executable() {
                @Override
                public void execute() {
                    resolver.setThemeName(request, response, "whatever");
                }
            });
            assertEquals("custom-theme", resolver.resolveThemeName(request));
        }
    }

    @Test
    public void verifyCustomTheme() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        registeredService.setTheme("custom-theme");
        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(registeredService);
        val plan = mock(AuthenticationServiceSelectionPlan.class);
        val service = RegisteredServiceTestUtils.getService();
        WebUtils.putServiceIntoFlowScope(context, service);
        when(plan.resolveService(any(Service.class))).thenReturn(service);
        val resolver = new RegisteredServiceThemeResolver(servicesManager, plan, new CasConfigurationProperties(), Map.of());
        resolver.setDefaultThemeName("example");
        assertEquals("custom-theme", resolver.resolveThemeName(request));
    }
}
