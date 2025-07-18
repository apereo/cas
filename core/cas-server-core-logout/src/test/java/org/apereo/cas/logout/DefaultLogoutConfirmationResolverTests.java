package org.apereo.cas.logout;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultLogoutConfirmationResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Logout")
class DefaultLogoutConfirmationResolverTests {

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
    @ExtendWith(CasTestExtension.class)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class BaseTests {
        @Autowired
        @Qualifier(LogoutConfirmationResolver.DEFAULT_BEAN_NAME)
        protected LogoutConfirmationResolver logoutConfirmationResolver;

        @Autowired
        protected ConfigurableApplicationContext applicationContext;
    }

    @Nested
    @TestPropertySource(properties = "cas.logout.confirm-logout=false")
    class ConfirmationDisabled extends BaseTests {
        @Test
        void verifyOperation() throws Exception {
            val context = MockRequestContext.create(applicationContext);
            assertTrue(logoutConfirmationResolver.isLogoutRequestConfirmed(context));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.logout.confirm-logout=true")
    class ConfirmationEnabled extends BaseTests {
        @Test
        void verifyOperation() throws Exception {
            val context = MockRequestContext.create(applicationContext);
            context.setParameter(WebUtils.REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED, "true");
            assertTrue(logoutConfirmationResolver.isLogoutRequestConfirmed(context));
        }
    }
}
