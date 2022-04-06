package org.apereo.cas.config;

import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasEurekaDiscoveryClientConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "CasEurekaDiscoveryClientConfiguration", proxyBeanMethods = false)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Discovery, module = "eureka")
public class CasEurekaDiscoveryClientConfiguration {
}
