package org.apereo.cas.services.web;

import org.apereo.cas.BaseThemeTests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import java.io.File;
import java.util.UUID;
import static java.nio.charset.StandardCharsets.*;
import static org.junit.jupiter.api.Assertions.*;

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
    class ExternalThemeTests extends BaseThemeTests {
        @Test
        void verifyCustomSource() throws Throwable {
            val context = MockRequestContext.create();

            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            registeredService.setTheme("my-theme");
            servicesManager.save(registeredService);

            val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
            WebUtils.putServiceIntoFlowScope(context, service);

            assertEquals("my-theme", themeResolver.resolveThemeName(context.getHttpServletRequest()));
        }
    }

    @SpringBootTest(classes = BaseThemeTests.SharedTestConfiguration.class,
        properties = "cas.theme.default-theme-name=example")
    @Nested
    class ExampleThemeTests extends BaseThemeTests {
        @Test
        void verifyNoAccess() throws Throwable {
            val context = MockRequestContext.create();
            context.addHeader("User-Agent", "Mozilla");

            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            val strategy = new DefaultRegisteredServiceAccessStrategy(false, true);
            registeredService.setAccessStrategy(strategy);
            servicesManager.save(registeredService);
            val service = RegisteredServiceTestUtils.getService();
            WebUtils.putServiceIntoFlowScope(context, service);
            assertEquals("example", themeResolver.resolveThemeName(context.getHttpServletRequest()));
        }

        @Test
        void verifyNoTheme() throws Throwable {
            val context = MockRequestContext.create();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            registeredService.setTheme(null);
            servicesManager.save(registeredService);

            val service = RegisteredServiceTestUtils.getService();
            WebUtils.putServiceIntoFlowScope(context, service);
            assertEquals("example", themeResolver.resolveThemeName(context.getHttpServletRequest()));
        }

        @Test
        void verifyGroovyTheme() throws Throwable {
            val context = MockRequestContext.create();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());

            val file = File.createTempFile("Theme", ".groovy");
            val script = IOUtils.toString(new ClassPathResource("GroovyTheme.groovy").getInputStream(), UTF_8);
            FileUtils.writeStringToFile(file, script, UTF_8);
            registeredService.setTheme(file.getCanonicalPath());
            servicesManager.save(registeredService);
            val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
            WebUtils.putServiceIntoFlowScope(context, service);
            assertEquals("some-theme", themeResolver.resolveThemeName(context.getHttpServletRequest()));
        }

        @Test
        void verifyUrlTheme() throws Throwable {
            try (val webServer = new MockWebServer("custom-theme")) {
                webServer.start();
                val context = MockRequestContext.create();
                val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
                registeredService.setTheme("http://localhost:%s".formatted(webServer.getPort()));
                servicesManager.save(registeredService);
                val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
                WebUtils.putServiceIntoFlowScope(context, service);

                assertDoesNotThrow(() -> themeResolver.setThemeName(context.getHttpServletRequest(),
                    context.getHttpServletResponse(), "whatever"));
                assertEquals("custom-theme", themeResolver.resolveThemeName(context.getHttpServletRequest()));
            }
        }

        @Test
        void verifyCustomTheme() throws Throwable {
            val context = MockRequestContext.create();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            registeredService.setTheme("custom-theme");
            servicesManager.save(registeredService);
            val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
            WebUtils.putServiceIntoFlowScope(context, service);
            assertEquals("custom-theme", themeResolver.resolveThemeName(context.getHttpServletRequest()));
        }
    }
}
