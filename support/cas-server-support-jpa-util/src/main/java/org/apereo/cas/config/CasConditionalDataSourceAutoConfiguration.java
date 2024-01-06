package org.apereo.cas.config;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * This is {@link CasConditionalDataSourceAutoConfiguration}.
 * This will import {@link DataSourceAutoConfiguration}
 * but only conditionally, if a datasource url is found in properties.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.JDBC)
@ConditionalOnProperty(name = "spring.datasource.url")
@SuppressWarnings("ConditionalOnProperty")
@ImportAutoConfiguration(DataSourceAutoConfiguration.class)
@AutoConfiguration
public class CasConditionalDataSourceAutoConfiguration {}
