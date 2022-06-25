package org.apereo.cas.config;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * This is {@link CasConsulDiscoveryClientConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Discovery, module = "consul")
@AutoConfiguration
public class CasConsulDiscoveryClientConfiguration {}
