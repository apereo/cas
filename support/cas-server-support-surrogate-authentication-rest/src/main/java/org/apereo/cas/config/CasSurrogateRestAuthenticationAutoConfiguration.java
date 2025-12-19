package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateRestAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasSurrogateRestAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SurrogateAuthentication, module = "rest")
@AutoConfiguration
public class CasSurrogateRestAuthenticationAutoConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "restfulSurrogateAuthenticationService")
    public BeanSupplier<SurrogateAuthenticationService> restfulSurrogateAuthenticationService(
        @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
        final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return BeanSupplier.of(SurrogateAuthenticationService.class)
            .alwaysMatch()
            .supply(Unchecked.supplier(() -> {
                val su = casProperties.getAuthn().getSurrogate();
                LOGGER.debug("Using REST endpoint [{}] with method [{}] to locate surrogate accounts",
                    su.getRest().getUrl(), su.getRest().getMethod());
                return new SurrogateRestAuthenticationService(servicesManager, casProperties,
                    principalAccessStrategyEnforcer, applicationContext);
            }))
            .otherwiseNull();
    }
}
