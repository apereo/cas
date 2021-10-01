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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * X509 Rest configuration class.
 *
 * @author Dmytro Fedonin
 * @since 5.1.0
 */
@Configuration(value = "x509RestConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class X509RestConfiguration {

    @Configuration(value = "X509RestCredentialFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class X509RestCredentialFactoryConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "x509RestMultipartBody")
        public RestHttpRequestCredentialFactory x509RestMultipartBody() {
            return new X509RestMultipartBodyCredentialFactory();
        }

        @Bean
        @ConditionalOnMissingBean(name = "x509RestRequestHeader")
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RestHttpRequestCredentialFactory x509RestRequestHeader(
            @Qualifier("x509CertificateExtractor")
            final X509CertificateExtractor x509CertificateExtractor) {
            return new X509RestHttpRequestHeaderCredentialFactory(x509CertificateExtractor);
        }

        @ConditionalOnProperty(prefix = "cas.rest.x509", name = "tls-client-auth", havingValue = "true")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RestHttpRequestCredentialFactory x509RestTlsClientCert() {
            return new X509RestTlsClientCertCredentialFactory();
        }

    }

    @Configuration(value = "X509RestCredentialFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class X509RestCredentialFactoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        @ConditionalOnMissingBean(name = "x509RestHttpRequestCredentialFactoryConfigurer")
        public RestHttpRequestCredentialFactoryConfigurer x509RestHttpRequestCredentialFactoryConfigurer(
            @Qualifier("x509RestTlsClientCert")
            final ObjectProvider<RestHttpRequestCredentialFactory> x509RestTlsClientCert,
            @Qualifier("x509RestMultipartBody")
            final ObjectProvider<RestHttpRequestCredentialFactory> x509RestMultipartBody,
            @Qualifier("x509RestRequestHeader")
            final ObjectProvider<RestHttpRequestCredentialFactory> x509RestRequestHeader,
            @Qualifier("x509CertificateExtractor")
            final X509CertificateExtractor x509CertificateExtractor,
            final CasConfigurationProperties casProperties) {
            return factory -> {
                val restProperties = casProperties.getRest().getX509();
                val headerAuth = restProperties.isHeaderAuth();
                val bodyAuth = restProperties.isBodyAuth();
                val tlsClientAuth = restProperties.isTlsClientAuth();

                if (tlsClientAuth && (headerAuth || bodyAuth)) {
                    LOGGER.warn("The X.509 feature over REST using \"headerAuth\" or \"bodyAuth\" provides a tremendously "
                                + "convenient target for claiming user identities or obtaining TGTs without proof of private "
                                + "key ownership. To securely use this feature, network configuration MUST allow connections "
                                + "to the CAS server only from trusted hosts which in turn have strict security limitations "
                                + "and logging. Thus, \"tlsClientAuth\" shouldn't be activated together with \"headerAuth\" "
                                + "or \"bodyAuth\"");
                }

                if (headerAuth) {
                    x509RestRequestHeader.ifAvailable(factory::registerCredentialFactory);
                }
                if (bodyAuth) {
                    x509RestRequestHeader.ifAvailable(factory::registerCredentialFactory);
                }
                if (tlsClientAuth) {
                    x509RestTlsClientCert.ifAvailable(factory::registerCredentialFactory);
                }
            };
        }

    }
}
