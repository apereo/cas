package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnCasFeatureModule;
import org.apereo.cas.web.support.JdbcThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import javax.sql.DataSource;

/**
 * This is {@link CasJdbcThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "CasJdbcThrottlingConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnCasFeatureModule(feature = CasFeatureModule.FeatureCatalog.Throttling, module = "jdbc")
public class CasJdbcThrottlingConfiguration {
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

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jdbcAuthenticationThrottle")
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("inspektrThrottleDataSource")
        final DataSource inspektrThrottleDataSource,
        @Qualifier("authenticationThrottlingConfigurationContext")
        final ThrottledSubmissionHandlerConfigurationContext ctx,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(ThrottledSubmissionHandlerInterceptor.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val throttle = casProperties.getAuthn().getThrottle();
                return new JdbcThrottledSubmissionHandlerInterceptorAdapter(
                    ctx, inspektrThrottleDataSource, throttle.getJdbc().getAuditQuery());
            })
            .otherwise(ThrottledSubmissionHandlerInterceptor::noOp)
            .get();
    }
}
