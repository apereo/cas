package org.apereo.cas.config.support.authentication;

import org.apereo.cas.adaptors.yubikey.YubiKeyCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link YubiKeyComponentSerializationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "yubikeyComponentSerializationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class YubiKeyComponentSerializationConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "yubikeyComponentSerializationPlanConfigurer")
    public ComponentSerializationPlanConfigurer yubikeyComponentSerializationPlanConfigurer() {
        return plan -> plan.registerSerializableClass(YubiKeyCredential.class);
    }
}
