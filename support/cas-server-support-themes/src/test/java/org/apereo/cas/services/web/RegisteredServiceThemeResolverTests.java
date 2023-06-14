package org.apereo.cas.services.web;

import org.apereo.cas.BaseThemeTests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.util.UUID;

import static java.nio.charset.StandardCharsets.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

/**
 * This is {@link RegisteredServiceThemeResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Web")
class RegisteredServiceThemeResolverTests {

    @SpringBootTest(classes = BaseThemeTests.SharedTestConfiguration.class,
        properties = {
            "cas.view.template-prefixes[0]=classpath:/ext-templates",
            "cas.theme.default-theme-name=example"
        })
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    class ExternalThemeTests extends BaseThemeTests {
        @Test
        void verifyCustomSource() {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            registeredService.setTheme("my-theme");
            servicesManager.save(registeredService);

            val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
            WebUtils.putServiceIntoFlowScope(context, service);

            assertEquals("my-theme", themeResolver.resolveThemeName(request));
        }
    }

    @SpringBootTest(classes = BaseThemeTests.SharedTestConfiguration.class,
        properties = "cas.theme.default-theme-name=example")
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    class ExampleThemeTests extends BaseThemeTests {
        @Test
        void verifyNoAccess() {
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
            servicesManager.save(registeredService);
            val service = RegisteredServiceTestUtils.getService();
            WebUtils.putServiceIntoFlowScope(context, service);
            assertEquals("example", themeResolver.resolveThemeName(request));
        }

        @Test
        void verifyNoTheme() {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            registeredService.setTheme(null);
            servicesManager.save(registeredService);

            val service = RegisteredServiceTestUtils.getService();
            WebUtils.putServiceIntoFlowScope(context, service);
            assertEquals("example", themeResolver.resolveThemeName(request));
        }

        @Test
        void verifyGroovyTheme() throws Exception {
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
            servicesManager.save(registeredService);
            val service = RegisteredServiceTestUtils.getService();
            WebUtils.putServiceIntoFlowScope(context, service);
            assertEquals("some-theme", themeResolver.resolveThemeName(request));
        }

        @Test
        void verifyUrlTheme() {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            registeredService.setTheme("http://localhost:6315");
            servicesManager.save(registeredService);
            val service = RegisteredServiceTestUtils.getService();
            WebUtils.putServiceIntoFlowScope(context, service);

            try (val webServer = new MockWebServer(6315,
                new ByteArrayResource("custom-theme".getBytes(UTF_8), "Output"), OK)) {
                webServer.start();
                assertDoesNotThrow(() -> themeResolver.setThemeName(request, response, "whatever"));
                assertEquals("custom-theme", themeResolver.resolveThemeName(request));
            }
        }

        @Test
        void verifyCustomTheme() {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            registeredService.setTheme("custom-theme");
            servicesManager.save(registeredService);
            val service = RegisteredServiceTestUtils.getService();
            WebUtils.putServiceIntoFlowScope(context, service);
            assertEquals("custom-theme", themeResolver.resolveThemeName(request));
        }
    }
}
