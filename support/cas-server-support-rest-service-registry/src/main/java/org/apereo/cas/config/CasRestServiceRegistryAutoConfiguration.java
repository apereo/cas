package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.RestfulServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServiceRegistryListener;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link CasRestServiceRegistryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "rest")
@AutoConfiguration
public class CasRestServiceRegistryAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.service-registry.rest.url").isUrl();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "restfulServiceRegistry")
    public ServiceRegistry restfulServiceRegistry(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<@NonNull List<ServiceRegistryListener>> serviceRegistryListeners,
        final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(ServiceRegistry.class)
            .when(CONDITION.given(applicationContext))
            .supply(() -> {
                val registry = casProperties.getServiceRegistry().getRest();
                LOGGER.debug("Creating REST-based service registry using endpoint [{}]", registry.getUrl());
                return new RestfulServiceRegistry(applicationContext,
                    Optional.ofNullable(serviceRegistryListeners.getIfAvailable()).orElseGet(ArrayList::new), registry);
            })
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "restfulServiceRegistryExecutionPlanConfigurer")
    public ServiceRegistryExecutionPlanConfigurer restfulServiceRegistryExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("restfulServiceRegistry")
        final ServiceRegistry restfulServiceRegistry) {
        return BeanSupplier.of(ServiceRegistryExecutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> {
                val registry = casProperties.getServiceRegistry().getRest();
                if (StringUtils.isNotBlank(registry.getUrl())) {
                    plan.registerServiceRegistry(restfulServiceRegistry);
                }
            })
            .otherwiseProxy()
            .get();
    }
}
