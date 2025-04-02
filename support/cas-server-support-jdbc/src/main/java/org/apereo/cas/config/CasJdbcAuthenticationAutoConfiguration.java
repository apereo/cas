package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasJdbcAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "jdbc")
@AutoConfiguration
@Import({
    CasJdbcBindAuthenticationConfiguration.class,
    CasJdbcQueryAuthenticationConfiguration.class,
    CasJdbcQueryEncodeAuthenticationConfiguration.class,
    CasJdbcSearchAuthenticationConfiguration.class,
    CasJdbcStoredProcedureAuthenticationConfiguration.class,
    CasJdbcAuthenticationMultitenancyConfiguration.class,
    CasPersonDirectoryJdbcConfiguration.class
})
public class CasJdbcAuthenticationAutoConfiguration {
}


