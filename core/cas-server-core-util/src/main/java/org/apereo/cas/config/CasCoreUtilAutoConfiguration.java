package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is {@link CasCoreUtilAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableScheduling
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core)
@AutoConfiguration
@Import({
    CasCoreUtilConfiguration.class,
    CasCoreUtilSerializationConfiguration.class
})
public class CasCoreUtilAutoConfiguration {
}
