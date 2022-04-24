package org.apereo.cas.config;

import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * This is {@link CasEurekaDiscoveryClientConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Discovery, module = "eureka")
@AutoConfiguration
public class CasEurekaDiscoveryClientConfiguration {}
