package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.web.support.JdbcThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@Configuration(value = "casJdbcThrottlingConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasThrottlingConfiguration.class)
public class CasJdbcThrottlingConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "inspektrThrottleDataSource")
    public DataSource inspektrThrottleDataSource(
        final CasConfigurationProperties casProperties) {
        return JpaBeans.newDataSource(casProperties.getAuthn().getThrottle().getJdbc());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle(
        @Qualifier("inspektrThrottleDataSource")
        final DataSource inspektrThrottleDataSource,
        @Qualifier("authenticationThrottlingConfigurationContext")
        final ThrottledSubmissionHandlerConfigurationContext authenticationThrottlingConfigurationContext,
        final CasConfigurationProperties casProperties) {
        val throttle = casProperties.getAuthn().getThrottle();
        return new JdbcThrottledSubmissionHandlerInterceptorAdapter(
            authenticationThrottlingConfigurationContext, inspektrThrottleDataSource, throttle.getJdbc().getAuditQuery());
    }
}
