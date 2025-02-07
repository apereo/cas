package org.apereo.cas.mfa.simple.validation;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import static org.junit.jupiter.api.Assertions.*;


/**
 * This is {@link DefaultCasSimpleMultifactorAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTest(classes = {
    DefaultCasSimpleMultifactorAuthenticationServiceTests.DefaultCasSimpleMultifactorAuthenticationServiceTestConfiguration.class,
    BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
class DefaultCasSimpleMultifactorAuthenticationServiceTests {
    @Autowired
    @Qualifier(CasSimpleMultifactorAuthenticationService.BEAN_NAME)
    private CasSimpleMultifactorAuthenticationService multifactorAuthenticationService;

    @Test
    void verifyOperation() throws Exception {
        val attributes = CollectionUtils.<String, Object>wrap("email", "casuser@example.org");
        assertDoesNotThrow(() -> multifactorAuthenticationService.update(RegisteredServiceTestUtils.getPrincipal(), attributes));
        assertTrue(attributes.containsKey("updated"));
    }

    @TestConfiguration(value = "DefaultCasSimpleMultifactorAuthenticationServiceTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DefaultCasSimpleMultifactorAuthenticationServiceTestConfiguration {
        @Bean
        public CasSimpleMultifactorAuthenticationAccountService casSimpleMultifactorAuthenticationAccountService() {
            return (principal, attributes) -> {
                assertTrue(attributes.containsKey("email"));
                attributes.put("updated", true);
            };
        }
    }

}
