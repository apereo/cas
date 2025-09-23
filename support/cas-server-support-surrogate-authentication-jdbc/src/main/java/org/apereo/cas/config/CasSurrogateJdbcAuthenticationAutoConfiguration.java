package org.apereo.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateJdbcAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;

/**
 * This is {@link CasSurrogateJdbcAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SurrogateAuthentication, module = "jdbc")
@AutoConfiguration
public class CasSurrogateJdbcAuthenticationAutoConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "jdbcSurrogateAuthenticationService")
    public BeanSupplier<SurrogateAuthenticationService> jdbcSurrogateAuthenticationService(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
        final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
        final CasConfigurationProperties casProperties,
        @Qualifier("surrogateAuthenticationJdbcDataSource")
        final DataSource surrogateAuthenticationJdbcDataSource,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val su = casProperties.getAuthn().getSurrogate();
        return BeanSupplier.of(SurrogateAuthenticationService.class)
            .when(() -> StringUtils.isNotBlank(su.getJdbc().getSurrogateSearchQuery()))
            .supply(Unchecked.supplier(() -> new SurrogateJdbcAuthenticationService(
                new JdbcTemplate(surrogateAuthenticationJdbcDataSource),
                servicesManager, casProperties, principalAccessStrategyEnforcer, applicationContext)))
            .otherwiseNull();
    }

    @Bean
    @ConditionalOnMissingBean(name = "surrogateAuthenticationJdbcDataSource")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DataSource surrogateAuthenticationJdbcDataSource(final CasConfigurationProperties casProperties) {
        val su = casProperties.getAuthn().getSurrogate();
        return JpaBeans.newDataSource(su.getJdbc());
    }
}
