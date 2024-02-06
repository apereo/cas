package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasPersonDirectoryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PersonDirectory)
@AutoConfiguration
@Import({
    CasPersonDirectoryConfiguration.class,
    CasPersonDirectoryJsonConfiguration.class,
    CasPersonDirectoryRestConfiguration.class,
    CasPersonDirectoryGroovyConfiguration.class,
    CasPersonDirectoryStubConfiguration.class
})
public class CasPersonDirectoryAutoConfiguration {
}
