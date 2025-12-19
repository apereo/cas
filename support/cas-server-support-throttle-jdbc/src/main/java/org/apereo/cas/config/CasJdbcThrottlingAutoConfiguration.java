package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.throttle.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.support.JdbcThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import module java.sql;

/**
 * This is {@link CasJdbcThrottlingAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Throttling, module = "jdbc")
@AutoConfiguration
public class CasJdbcThrottlingAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.throttle.jdbc.enabled").isTrue().evenIfMissing();

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "inspektrThrottleDataSource")
    public DataSource inspektrThrottleDataSource(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(DataSource.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> JpaBeans.newDataSource(casProperties.getAuthn().getThrottle().getJdbc()))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "inspektrThrottleJdbcTemplate")
    public JdbcOperations inspektrThrottleJdbcTemplate(
        @Qualifier("inspektrThrottleDataSource")
        final DataSource inspektrThrottleDataSource,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(JdbcOperations.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new JdbcTemplate(inspektrThrottleDataSource))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jdbcAuthenticationThrottle")
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("inspektrThrottleJdbcTemplate")
        final JdbcOperations inspektrThrottleJdbcTemplate,
        @Qualifier("authenticationThrottlingConfigurationContext")
        final ThrottledSubmissionHandlerConfigurationContext ctx) {
        return BeanSupplier.of(ThrottledSubmissionHandlerInterceptor.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new JdbcThrottledSubmissionHandlerInterceptorAdapter(ctx, inspektrThrottleJdbcTemplate))
            .otherwise(ThrottledSubmissionHandlerInterceptor::noOp)
            .get();
    }
}
