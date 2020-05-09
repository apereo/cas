package org.apereo.cas.support.x509.rest.config;

import org.apereo.cas.adaptors.x509.authentication.X509CertificateExtractor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;
import org.apereo.cas.support.x509.rest.X509RestHttpRequestHeaderCredentialFactory;
import org.apereo.cas.support.x509.rest.X509RestMultipartBodyCredentialFactory;
import org.apereo.cas.support.x509.rest.X509RestTlsClientCertCredentialFactory;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
public class X509RestConfiguration {

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
        return new X509RestHttpRequestHeaderCredentialFactory(x509CertificateExtractor.getObject());
    }

    @ConditionalOnProperty(prefix = "cas.rest", name = "tls-client-auth", havingValue = "true")
    @Bean
    public RestHttpRequestCredentialFactory x509RestTlsClientCert() {
        return new X509RestTlsClientCertCredentialFactory();
    }

    @Bean
    public RestHttpRequestCredentialFactoryConfigurer x509RestHttpRequestCredentialFactoryConfigurer() {
        return factory -> {
            val restProperties = casProperties.getRest();
            val extractor = x509CertificateExtractor.getObject();
            val headerAuth = restProperties.isHeaderAuth();
            val bodyAuth = restProperties.isBodyAuth();
            val tlsClientAuth = restProperties.isTlsClientAuth();
            LOGGER.trace("Is certificate extractor available? = [{}], headerAuth = [{}], bodyAuth = [{}], tlsClientAuth = [{}]",
                extractor, headerAuth, bodyAuth, tlsClientAuth);

            if (tlsClientAuth && (headerAuth || bodyAuth)) {
                LOGGER.warn("The X.509 feature over REST using \"headerAuth\" or \"bodyAuth\" provides a tremendously "
                    + "convenient target for claiming user identities or obtaining TGTs without proof of private "
                    + "key ownership. To securely use this feature, network configuration MUST allow connections "
                    + "to the CAS server only from trusted hosts which in turn have strict security limitations "
                    + "and logging. Thus, \"tlsClientAuth\" shouldn't be activated together with \"headerAuth\" "
                    + "or \"bodyAuth\"");
            }

            if (extractor != null && headerAuth) {
                factory.registerCredentialFactory(x509RestRequestHeader());
            }
            if (bodyAuth) {
                factory.registerCredentialFactory(x509RestMultipartBody());
            }
            if (tlsClientAuth) {
                factory.registerCredentialFactory(x509RestTlsClientCert());
            }
        };
    }

}
