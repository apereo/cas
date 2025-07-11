package org.apereo.cas.services.web;

import org.apereo.cas.BaseThemeTests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import java.nio.file.Files;
import java.util.UUID;
import static java.nio.charset.StandardCharsets.UTF_8;
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
            "spring.web.resources.chain.strategy.content.enabled=true",
            "cas.view.template-prefixes[0]=classpath:/ext-templates",
            "cas.theme.default-theme-name=example"
        })
    @Nested
    @ExtendWith(CasTestExtension.class)
    class ExternalThemeTests extends BaseThemeTests {
        @Test
        void verifyCustomSource() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            registeredService.setTheme("my-theme");
            servicesManager.save(registeredService);

            val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
            WebUtils.putServiceIntoFlowScope(context, service);

            assertEquals("my-theme", themeResolver.resolveThemeName(context.getHttpServletRequest()));
        }
    }

    @SpringBootTest(classes = BaseThemeTests.SharedTestConfiguration.class, properties = "cas.theme.default-theme-name=example")
    @Nested
    @ExtendWith(CasTestExtension.class)
    class ExampleThemeTests extends BaseThemeTests {
        @Test
        void verifyNoAccess() throws Throwable {
            val context = MockRequestContext.create(applicationContext).withUserAgent().setClientInfo();
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
            val context = MockRequestContext.create(applicationContext);
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            registeredService.setTheme(null);
            servicesManager.save(registeredService);

            val service = RegisteredServiceTestUtils.getService();
            WebUtils.putServiceIntoFlowScope(context, service);
            assertEquals("example", themeResolver.resolveThemeName(context.getHttpServletRequest()));
        }

        @Test
        void verifyGroovyTheme() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());

            val file = Files.createTempFile("Theme", ".groovy").toFile();
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
                val context = MockRequestContext.create(applicationContext);
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
            val context = MockRequestContext.create(applicationContext);
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            registeredService.setTheme("custom-theme");
            servicesManager.save(registeredService);
            val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
            WebUtils.putServiceIntoFlowScope(context, service);
            assertEquals("custom-theme", themeResolver.resolveThemeName(context.getHttpServletRequest()));
        }
    }
}
