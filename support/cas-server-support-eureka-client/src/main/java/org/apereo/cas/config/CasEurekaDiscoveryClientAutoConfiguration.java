package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.configuration.SSLContextFactory;
import org.springframework.cloud.configuration.TlsProperties;
import org.springframework.cloud.netflix.eureka.http.EurekaClientHttpRequestFactorySupplier;
import org.springframework.cloud.netflix.eureka.http.RestTemplateDiscoveryClientOptionalArgs;
import org.springframework.context.annotation.Bean;

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
    public AbstractDiscoveryClientOptionalArgs<Void> restTemplateDiscoveryClientOptionalArgs(
        final TlsProperties tlsProperties, final EurekaClientHttpRequestFactorySupplier factorySupplier,
        @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext) throws Exception {
        val result = new RestTemplateDiscoveryClientOptionalArgs(factorySupplier);

        if (tlsProperties.isEnabled()) {
            val factory = new SSLContextFactory(tlsProperties);
            result.setSSLContext(factory.createSSLContext());
        } else {
            result.setSSLContext(casSslContext.getSslContext());
            result.setHostnameVerifier(casSslContext.getHostnameVerifier());
        }
        return result;
    }

}
