package org.apereo.cas.support.x509.rest.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.ChainingRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.RestHttpRequestCredentialFactoryConfigurer;
import org.apereo.cas.support.x509.rest.X509RestHttpRequestCredentialFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Dmytro Fedonin
 * @since 5.1.0
 */
@Configuration("x509RestConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class X509RestConfiguration implements RestHttpRequestCredentialFactoryConfigurer {

    @Bean
    public RestHttpRequestCredentialFactory x509CredentialFactory() {
        return new X509RestHttpRequestCredentialFactory();
    }

    @Override
    public void registerCredentialFactory(final ChainingRestHttpRequestCredentialFactory factory) {
        factory.registerCredentialFactory(x509CredentialFactory());
    }
}
