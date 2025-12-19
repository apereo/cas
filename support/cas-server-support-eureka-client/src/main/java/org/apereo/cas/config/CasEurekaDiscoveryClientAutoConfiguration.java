package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.shared.transport.jersey.TransportClientFactories;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.configuration.SSLContextFactory;
import org.springframework.cloud.configuration.TlsProperties;
import org.springframework.cloud.netflix.eureka.TimeoutProperties;
import org.springframework.cloud.netflix.eureka.http.DefaultEurekaClientHttpRequestFactorySupplier;
import org.springframework.cloud.netflix.eureka.http.EurekaClientHttpRequestFactorySupplier;
import org.springframework.cloud.netflix.eureka.http.RestClientDiscoveryClientOptionalArgs;
import org.springframework.cloud.netflix.eureka.http.RestClientTransportClientFactories;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * This is {@link CasEurekaDiscoveryClientAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Discovery, module = "eureka")
@AutoConfiguration
public class CasEurekaDiscoveryClientAutoConfiguration {
    @Bean
    public AbstractDiscoveryClientOptionalArgs restClientDiscoveryClientOptionalArgs(
        final ConfigurableApplicationContext applicationContext,
        final TlsProperties tlsProperties,
        final TimeoutProperties restTemplateTimeoutProperties,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final ObjectProvider<RestClient.@NonNull Builder> restClientBuilderProvider) throws Exception {
        val customizers = new HashSet<>(applicationContext.getBeansOfType(
            EurekaClientHttpRequestFactorySupplier.RequestConfigCustomizer.class).values());
        val factorySupplier = new DefaultEurekaClientHttpRequestFactorySupplier(restTemplateTimeoutProperties, customizers);
        val result = new RestClientDiscoveryClientOptionalArgs(factorySupplier,
            () -> restClientBuilderProvider.getIfAvailable(RestClient::builder));

        if (tlsProperties.isEnabled()) {
            val factory = new SSLContextFactory(tlsProperties);
            result.setSSLContext(factory.createSSLContext());
        } else {
            result.setSSLContext(casSslContext.getSslContext());
            result.setHostnameVerifier(casSslContext.getHostnameVerifier());
        }
        return result;
    }

    @Bean
    public TransportClientFactories restClientTransportClientFactories(final RestClientDiscoveryClientOptionalArgs args) {
        return new RestClientTransportClientFactories(args);
    }
}
