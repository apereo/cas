package org.apereo.cas.config.support.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.yubikey.YubiKeyRestHttpRequestCredentialFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.ChainingRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.RestHttpRequestCredentialFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link YubiKeyRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("yubiKeyRestConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(value = RestHttpRequestCredentialFactoryConfigurer.class)
@Slf4j
public class YubiKeyRestConfiguration {

    @Bean
    public RestHttpRequestCredentialFactoryConfigurer googleAuthenticatorRestHttpRequestCredentialFactoryConfigurer() {
        return new RestHttpRequestCredentialFactoryConfigurer() {
            @Override
            public void registerCredentialFactory(final ChainingRestHttpRequestCredentialFactory factory) {
                factory.registerCredentialFactory(new YubiKeyRestHttpRequestCredentialFactory());
            }
        };
    }
}
