package org.apereo.cas.config;

import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasConsulDiscoveryClientConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration(value = "CasConsulDiscoveryClientConfiguration", proxyBeanMethods = false)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Discovery, module = "consul")
public class CasConsulDiscoveryClientConfiguration {
}
