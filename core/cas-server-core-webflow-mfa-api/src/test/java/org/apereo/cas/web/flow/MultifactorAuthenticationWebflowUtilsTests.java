package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationWebflowUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Webflow")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    MultifactorAuthenticationWebflowUtilsTests.MultifactorAuthenticationTestConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class MultifactorAuthenticationWebflowUtilsTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyCustomizers() {
        val results = MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext);
        assertFalse(results.isEmpty());
    }

    @Test
    void verifyRegistration() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        MultifactorAuthenticationWebflowUtils.putMultifactorDeviceRegistrationEnabled(requestContext, true);
        assertTrue(MultifactorAuthenticationWebflowUtils.isMultifactorDeviceRegistrationEnabled(requestContext));

        val ac = OneTimeTokenAccount.builder()
            .validationCode(123456)
            .username("casuser")
            .name("Example")
            .build();
        MultifactorAuthenticationWebflowUtils.putOneTimeTokenAccount(requestContext, ac);
        assertNotNull(MultifactorAuthenticationWebflowUtils.getOneTimeTokenAccount(requestContext, OneTimeTokenAccount.class));
        MultifactorAuthenticationWebflowUtils.putOneTimeTokenAccounts(requestContext, List.of(ac));
        assertNotNull(MultifactorAuthenticationWebflowUtils.getOneTimeTokenAccounts(requestContext));
    }
    
    @TestConfiguration(value = "MultifactorAuthenticationTestConfiguration", proxyBeanMethods = false)
    public static class MultifactorAuthenticationTestConfiguration {
        @Bean
        public CasMultifactorWebflowCustomizer sampleCustomizer() {
            return new CasMultifactorWebflowCustomizer() {
            };
        }
    }
}
