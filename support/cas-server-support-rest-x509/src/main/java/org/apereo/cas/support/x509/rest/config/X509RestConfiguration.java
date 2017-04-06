package org.apereo.cas.support.x509.rest.config;

import org.apereo.cas.support.rest.CredentialFactory;
import org.apereo.cas.support.x509.rest.X509CredentialFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Dmytro Fedonin
 * @since 5.1.0
 */
@Configuration("x509RestConfiguration")
public class X509RestConfiguration {

    @Bean
    public CredentialFactory x509CredentialFactory() {
        return new X509CredentialFactory();
    }
}
