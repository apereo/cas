package org.apereo.cas.logout;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultLogoutRedirectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Logout")
@ExtendWith(CasTestExtension.class)
class DefaultLogoutRedirectionStrategyTests {

    private static MockRequestContext getMockRequestContext() throws Exception {
        val context = MockRequestContext.create();
        context.setRequestURI(CasProtocolConstants.ENDPOINT_LOGOUT);
        return context;
    }

    @SpringBootTestAutoConfigurations
    @SpringBootTest(classes = {
        CasCoreLogoutAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreAutoConfiguration.class
    })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class BaseTests {
        @Autowired
        @Qualifier("defaultLogoutRedirectionStrategy")
        protected LogoutRedirectionStrategy defaultLogoutRedirectionStrategy;

        @Autowired
        @Qualifier(ServicesManager.BEAN_NAME)
        protected ServicesManager servicesManager;
    }

    @Nested
    @TestPropertySource(properties = "cas.logout.follow-service-redirects=true")
    class NoRedirectTests extends BaseTests {
        @Test
        void verifyNoRedirect() throws Throwable {
            val context = getMockRequestContext();
            assertTrue(defaultLogoutRedirectionStrategy.supports(context.getHttpServletRequest(), context.getHttpServletResponse()));
            var response = defaultLogoutRedirectionStrategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
            assertTrue(response.getLogoutRedirectUrl().isEmpty());

            WebUtils.putLogoutRedirectUrl(context.getHttpServletRequest(), "https://github.com/apereo/cas");
            response = defaultLogoutRedirectionStrategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
            assertFalse(response.getLogoutRedirectUrl().isEmpty());
            assertEquals("https://github.com/apereo/cas", response.getLogoutRedirectUrl().get());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.logout.follow-service-redirects=true")
    class RedirectTests extends BaseTests {
        @Test
        void verifyRedirectToTrustedUrl() throws Throwable {
            val context = getMockRequestContext();
            WebUtils.putLogoutRedirectUrl(context.getHttpServletRequest(), "https://github.com/apereo/cas");
            var response = defaultLogoutRedirectionStrategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
            assertFalse(response.getLogoutRedirectUrl().isEmpty());
            assertEquals("https://github.com/apereo/cas", response.getLogoutRedirectUrl().get());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.logout.follow-service-redirects=true",
        "cas.logout.redirect-parameter=targetParam"
    })
    class RedirectToServiceTests extends BaseTests {
        @Test
        void verifyRedirectToService() throws Throwable {
            val context = getMockRequestContext();
            context.setParameter("targetParam", "https://github.com/apereo/cas");

            var response = defaultLogoutRedirectionStrategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
            assertTrue(response.getLogoutRedirectUrl().isEmpty());

            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService("https://github.com/apereo/cas"));
            response = defaultLogoutRedirectionStrategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
            assertFalse(response.getLogoutRedirectUrl().isEmpty());
            assertEquals("https://github.com/apereo/cas", response.getLogoutRedirectUrl().get());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.logout.follow-service-redirects=true",
        "cas.view.default-redirect-url=https://google.com"
    })
    class DefaultServiceTests extends BaseTests {
        @Test
        void verifyRedirectToDefaultServiceInConfig() throws Throwable {
            val context = getMockRequestContext();
            servicesManager.save(RegisteredServiceTestUtils.getRegisteredService("https://google.com"));
            val response = defaultLogoutRedirectionStrategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
            assertFalse(response.getLogoutRedirectUrl().isEmpty());
            assertEquals("https://google.com", response.getLogoutRedirectUrl().get());
        }
    }
}
