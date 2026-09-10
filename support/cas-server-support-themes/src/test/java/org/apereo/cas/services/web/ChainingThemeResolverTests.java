package org.apereo.cas.services.web;

import module java.base;
import org.apereo.cas.BaseThemeTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingThemeResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Web")
class ChainingThemeResolverTests {

    @SpringBootTest(classes = BaseThemeTests.SharedTestConfiguration.class,
        properties = "cas.theme.default-theme-name=example")
    @Nested
    @ExtendWith(CasTestExtension.class)
    class ThemeDefinitionTests extends BaseThemeTests {

        @Test
        void verifyDefinedThemeFromRequestHeader() throws Throwable {
            val context = registerServiceWithTheme(null).addHeader("theme", "custom-theme");
            assertEquals("custom-theme", themeResolver.resolveThemeName(context.getHttpServletRequest()));
        }

        @Test
        void verifyUndefinedThemeFromRequestHeaderIsIgnored() throws Throwable {
            val context = registerServiceWithTheme(null).addHeader("theme", UUID.randomUUID().toString());
            assertEquals("example", themeResolver.resolveThemeName(context.getHttpServletRequest()));
        }

        @Test
        void verifyTraversingThemeFromRequestHeaderIsIgnored() throws Throwable {
            val context = registerServiceWithTheme(null).addHeader("theme", "../../../../etc/cas/config");
            assertEquals("example", themeResolver.resolveThemeName(context.getHttpServletRequest()));
        }

        @Test
        void verifyUndefinedThemeFromRequestHeaderYieldsToRegisteredService() throws Throwable {
            val context = registerServiceWithTheme("custom-theme").addHeader("theme", UUID.randomUUID().toString());
            assertEquals("custom-theme", themeResolver.resolveThemeName(context.getHttpServletRequest()));
        }

        @Test
        void verifyUndefinedThemeFromRestEndpointIsIgnored() throws Throwable {
            try (val webServer = new MockWebServer(UUID.randomUUID().toString())) {
                webServer.start();
                val context = registerServiceWithTheme("http://localhost:%s".formatted(webServer.getPort()));
                assertEquals("example", themeResolver.resolveThemeName(context.getHttpServletRequest()));
            }
        }

        private MockRequestContext registerServiceWithTheme(final String theme) throws Exception {
            val context = MockRequestContext.create(applicationContext);
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            registeredService.setTheme(theme);
            servicesManager.save(registeredService);
            val service = RegisteredServiceTestUtils.getService(registeredService.getServiceId());
            WebUtils.putServiceIntoFlowScope(context, service);
            return context;
        }
    }
}
