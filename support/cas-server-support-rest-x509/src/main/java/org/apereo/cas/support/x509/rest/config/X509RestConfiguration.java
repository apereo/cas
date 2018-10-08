package org.apereo.cas.support.x509.rest.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.factory.ChainingRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;
import org.apereo.cas.support.x509.rest.X509RestHttpRequestHeaderCredentialFactory;
import org.apereo.cas.support.x509.rest.X509RestMultipartBodyCredentialFactory;
import org.apereo.cas.web.extractcert.X509CertificateExtractor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author Dmytro Fedonin
 * @since 5.1.0
 */
@Configuration("x509RestConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class X509RestConfiguration implements RestHttpRequestCredentialFactoryConfigurer {
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("x509CertificateExtractor")
    @Lazy
    private ObjectProvider<X509CertificateExtractor> x509CertificateExtractor;

    @Bean
    public RestHttpRequestCredentialFactory x509RestMultipartBody() {
        return new X509RestMultipartBodyCredentialFactory();
    }

    @Bean
    public RestHttpRequestCredentialFactory x509RestRequestHeader() {
        return new X509RestHttpRequestHeaderCredentialFactory(x509CertificateExtractor.getIfAvailable());
    }
    
    @Override
    public void configureCredentialFactory(final ChainingRestHttpRequestCredentialFactory factory) {
        val restProperties = casProperties.getRest();
        val extractor = x509CertificateExtractor.getIfAvailable();
        val headerAuth = restProperties.isHeaderAuth();
        val bodyAuth = restProperties.isBodyAuth();
        LOGGER.debug("is certificate extractor available? = {}, headerAuth = {}, bodyAuth = {}",
            extractor, headerAuth, bodyAuth);
        if (extractor != null && headerAuth) {
            factory.registerCredentialFactory(x509RestRequestHeader());
        }
        if (bodyAuth) {
            factory.registerCredentialFactory(x509RestMultipartBody());
        }
    }
}
