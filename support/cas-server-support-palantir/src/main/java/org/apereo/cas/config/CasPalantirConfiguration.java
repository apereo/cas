package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.palantir.controller.DashboardController;
import org.apereo.cas.palantir.controller.SchemaController;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import java.util.List;

/**
 * This is {@link CasPalantirConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AdminConsole)
public class CasPalantirConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "palantirDashboardController")
    public DashboardController palantirDashboardController() {
        return new DashboardController();
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirSchemaController")
    public SchemaController palantirSchemaController() {
        return new SchemaController();
    }

    @Bean
    @ConditionalOnMissingBean(name = "palantirEndpointWebSecurityConfigurer")
    public ProtocolEndpointWebSecurityConfigurer<Void> palantirEndpointWebSecurityConfigurer() {
        return new ProtocolEndpointWebSecurityConfigurer<>() {
            @Override
            public List<String> getIgnoredEndpoints() {
                return List.of(
                    StringUtils.prependIfMissing(PalantirConstants.URL_PATH_PALANTIR, "/")
                );
            }
        };
    }

}
