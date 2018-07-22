package org.apereo.cas.support.x509.rest.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.factory.ChainingRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;
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
public class X509RestConfiguration implements RestHttpRequestCredentialFactoryConfigurer {

    @Bean
    public RestHttpRequestCredentialFactory x509CredentialFactory() {
        return new X509RestHttpRequestCredentialFactory();
    }

    @Override
    public void configureCredentialFactory(final ChainingRestHttpRequestCredentialFactory factory) {
        factory.registerCredentialFactory(x509CredentialFactory());
    }
}
