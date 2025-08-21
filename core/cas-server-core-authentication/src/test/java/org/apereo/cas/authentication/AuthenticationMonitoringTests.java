package org.apereo.cas.authentication;

import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationMonitoringTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Authentication")
@Slf4j
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    AuthenticationMonitoringTests.AuthenticationPlanTestConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class
})
@ExtendWith(CasTestExtension.class)
@AutoConfigureObservability
class AuthenticationMonitoringTests {
    @Autowired
    @Qualifier(AuthenticationManager.BEAN_NAME)
    private AuthenticationManager authenticationManager;

    @Test
    void verifyOperation() throws Throwable {
        var credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "P@$$word");
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(credential);
        val result = authenticationManager.authenticate(transaction);
        assertNotNull(result);
    }

    @TestConfiguration(value = "AuthenticationPlanTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class AuthenticationPlanTestConfiguration {
        @Bean
        public ServicesManager servicesManager() {
            return mock(ServicesManager.class);
        }

        @Bean
        public AuthenticationHandler myAuthenticationHandler() {
            return new AcceptUsersAuthenticationHandler(Map.of("casuser", "P@$$word"));
        }

        @Bean
        public AuthenticationEventExecutionPlanConfigurer cfg(
            @Qualifier("myAuthenticationHandler")
            final AuthenticationHandler myAuthenticationHandler) {
            return plan -> plan.registerAuthenticationHandler(myAuthenticationHandler);
        }
    }
}
