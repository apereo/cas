package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * This is {@link CasCoreServicesAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAsync(proxyTargetClass = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry)
@AutoConfiguration
@Import({
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreServicesMonitoringConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreServicesComponentSerializationConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasServiceRegistryInitializationConfiguration.class
})
public class CasCoreServicesAutoConfiguration {
}
