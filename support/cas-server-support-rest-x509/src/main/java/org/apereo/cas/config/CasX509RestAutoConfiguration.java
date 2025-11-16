package org.apereo.cas.config;

import org.apereo.cas.adaptors.x509.authentication.X509CertificateExtractor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;
import org.apereo.cas.support.x509.rest.X509RestHttpRequestHeaderCredentialFactory;
import org.apereo.cas.support.x509.rest.X509RestMultipartBodyCredentialFactory;
import org.apereo.cas.support.x509.rest.X509RestTlsClientCertCredentialFactory;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * X509 Rest configuration class.
 *
 * @author Dmytro Fedonin
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.X509, module = "rest")
@AutoConfiguration
public class CasX509RestAutoConfiguration {

    @Configuration(value = "X509RestCredentialFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class X509RestCredentialFactoryConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "x509RestMultipartBody")
        public RestHttpRequestCredentialFactory x509RestMultipartBody(final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(RestHttpRequestCredentialFactory.class)
                .when(BeanCondition.on("cas.rest.x509.body-auth").isTrue().given(applicationContext.getEnvironment()))
                .supply(X509RestMultipartBodyCredentialFactory::new)
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "x509RestRequestHeader")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RestHttpRequestCredentialFactory x509RestRequestHeader(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("x509CertificateExtractor")
            final X509CertificateExtractor x509CertificateExtractor) {
            return BeanSupplier.of(RestHttpRequestCredentialFactory.class)
                .when(BeanCondition.on("cas.rest.x509.header-auth").isTrue().given(applicationContext.getEnvironment()))
                .supply(() -> new X509RestHttpRequestHeaderCredentialFactory(x509CertificateExtractor))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "x509RestTlsClientCert")
        public RestHttpRequestCredentialFactory x509RestTlsClientCert(
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(RestHttpRequestCredentialFactory.class)
                .when(BeanCondition.on("cas.rest.x509.tls-client-auth").isTrue().given(applicationContext.getEnvironment()))
                .supply(X509RestTlsClientCertCredentialFactory::new)
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "X509RestCredentialFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class X509RestCredentialFactoryPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "x509RestHttpRequestCredentialFactoryConfigurer")
        public RestHttpRequestCredentialFactoryConfigurer x509RestHttpRequestCredentialFactoryConfigurer(
            @Qualifier("x509RestTlsClientCert")
            final ObjectProvider<@NonNull RestHttpRequestCredentialFactory> x509RestTlsClientCert,
            @Qualifier("x509RestMultipartBody")
            final ObjectProvider<@NonNull RestHttpRequestCredentialFactory> x509RestMultipartBody,
            @Qualifier("x509RestRequestHeader")
            final ObjectProvider<@NonNull RestHttpRequestCredentialFactory> x509RestRequestHeader,
            final CasConfigurationProperties casProperties) {
            return factory -> {
                val restProperties = casProperties.getRest().getX509();
                val headerAuth = restProperties.isHeaderAuth();
                val bodyAuth = restProperties.isBodyAuth();
                val tlsClientAuth = restProperties.isTlsClientAuth();

                if (tlsClientAuth && (headerAuth || bodyAuth)) {
                    LOGGER.warn("The X.509 feature over REST using header/body authentication provides a tremendously "
                                + "convenient target for claiming user identities or obtaining TGTs without proof of private "
                                + "key ownership. To securely use this feature, network configuration MUST allow connections "
                                + "to the CAS server only from trusted hosts which in turn have strict security limitations "
                                + "and logging. Thus, TLS authentication shouldn't be activated together with header "
                                + "or body authentication.");
                }

                if (headerAuth) {
                    x509RestRequestHeader.ifAvailable(factory::registerCredentialFactory);
                }
                if (bodyAuth) {
                    x509RestMultipartBody.ifAvailable(factory::registerCredentialFactory);
                }
                if (tlsClientAuth) {
                    x509RestTlsClientCert.ifAvailable(factory::registerCredentialFactory);
                }
            };
        }

    }
}
