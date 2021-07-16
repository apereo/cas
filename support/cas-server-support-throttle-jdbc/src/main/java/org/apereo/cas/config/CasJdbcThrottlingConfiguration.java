package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.web.support.JdbcThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerConfigurationContext;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * This is {@link CasJdbcThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casJdbcThrottlingConfiguration", proxyBeanMethods = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasThrottlingConfiguration.class)
public class CasJdbcThrottlingConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationThrottlingConfigurationContext")
    private ObjectProvider<ThrottledSubmissionHandlerConfigurationContext> authenticationThrottlingConfigurationContext;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "inspektrThrottleDataSource")
    public DataSource inspektrThrottleDataSource() {
        return JpaBeans.newDataSource(casProperties.getAuthn().getThrottle().getJdbc());
    }

    @Bean
    @RefreshScope
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle() {
        val throttle = casProperties.getAuthn().getThrottle();
        return new JdbcThrottledSubmissionHandlerInterceptorAdapter(
            authenticationThrottlingConfigurationContext.getObject(),
            inspektrThrottleDataSource(), throttle.getJdbc().getAuditQuery());
    }
}
