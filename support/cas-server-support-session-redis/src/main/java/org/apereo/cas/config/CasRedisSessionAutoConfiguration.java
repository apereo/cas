package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * This is {@link CasRedisSessionAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableRedisHttpSession
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SessionManagement, module = "redis")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
public class CasRedisSessionAutoConfiguration {}
