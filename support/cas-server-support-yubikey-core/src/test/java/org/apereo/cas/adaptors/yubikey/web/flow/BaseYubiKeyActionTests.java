package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.adaptors.yubikey.YubiKeyMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BaseYubiKeyActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class
})
@Import(BaseYubiKeyActionTests.MultifactorAuthenticationTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTestAutoConfigurations
public abstract class BaseYubiKeyActionTests {
    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    protected TenantExtractor tenantExtractor;

    @TestConfiguration(value = "MultifactorAuthenticationTestConfiguration", proxyBeanMethods = false)
    public static class MultifactorAuthenticationTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider yubikeyProvider() {
            return new YubiKeyMultifactorAuthenticationProvider();
        }

        @Bean
        public MultifactorAuthenticationPrincipalResolver yubikeyMfaPrincipalResolver() {
            return MultifactorAuthenticationPrincipalResolver.identical();
        }
    }
}
