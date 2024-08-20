package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasAmazonCloudWatchAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring, module = "aws")
@AutoConfiguration
@Import({
    CasAmazonCloudWatchLoggingConfiguration.class,
    CasAmazonCloudWatchMetricsConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasAmazonCloudWatchAutoConfiguration {
}
