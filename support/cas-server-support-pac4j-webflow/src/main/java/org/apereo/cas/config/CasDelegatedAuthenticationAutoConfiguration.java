package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasDelegatedAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication)
@AutoConfiguration
@Import({
    DelegatedAuthenticationEventExecutionPlanConfiguration.class,
    DelegatedAuthenticationConfiguration.class,
    DelegatedAuthenticationSerializationConfiguration.class,
    DelegatedAuthenticationProvisioningConfiguration.class,
    DelegatedAuthenticationWebflowConfiguration.class,
    DelegatedAuthenticationDynamicDiscoverySelectionConfiguration.class,
    DelegatedAuthenticationProfileSelectionConfiguration.class
})
public class CasDelegatedAuthenticationAutoConfiguration {
}
