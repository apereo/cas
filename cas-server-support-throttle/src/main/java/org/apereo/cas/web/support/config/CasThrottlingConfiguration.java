package org.apereo.cas.web.support.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.AbstractThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionCleaner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link CasThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casThrottlingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasThrottlingConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @ConditionalOnMissingBean(name = "authenticationThrottle")
    @Bean(name = {"defaultAuthenticationThrottle", "authenticationThrottle"})
    public HandlerInterceptorAdapter defaultAuthenticationThrottle() {
        if (StringUtils.isNotBlank(casProperties.getAuthn().getThrottle().getUsernameParameter())) {
            return inMemoryIpAddressUsernameThrottle();
        }

        if (casProperties.getAuthn().getThrottle().getFailure().getThreshold() > 0
                && casProperties.getAuthn().getThrottle().getFailure().getRangeSeconds() > 0) {
            return inMemoryIpAddressThrottle();
        }
        return neverThrottle();
    }

    @Bean
    @RefreshScope
    public HandlerInterceptorAdapter inMemoryIpAddressUsernameThrottle() {
        return configureInMemoryInterceptorAdaptor(
                new InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter());
    }

    @Bean
    public HandlerInterceptorAdapter inMemoryIpAddressThrottle() {
        return configureInMemoryInterceptorAdaptor(
                new InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter());
    }

    @Bean
    public HandlerInterceptorAdapter neverThrottle() {
        return new HandlerInterceptorAdapter() {
            @Override
            public boolean preHandle(final HttpServletRequest request,
                                     final HttpServletResponse response,
                                     final Object handler) throws Exception {
                return true;
            }
        };
    }

    private AbstractThrottledSubmissionHandlerInterceptorAdapter
    configureThrottleHandlerInterceptorAdaptor(final AbstractThrottledSubmissionHandlerInterceptorAdapter interceptorAdapter) {
        interceptorAdapter.setUsernameParameter(casProperties.getAuthn().getThrottle().getUsernameParameter());
        interceptorAdapter.setFailureThreshold(casProperties.getAuthn().getThrottle().getFailure().getThreshold());
        interceptorAdapter.setFailureRangeInSeconds(casProperties.getAuthn().getThrottle().getFailure().getRangeSeconds());
        return interceptorAdapter;
    }

    private HandlerInterceptorAdapter
    configureInMemoryInterceptorAdaptor(final AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter interceptorAdapter) {
        return configureThrottleHandlerInterceptorAdaptor(interceptorAdapter);
    }

    @Bean
    public Runnable inMemoryThrottledSubmissionCleaner(@Qualifier("authenticationThrottle")
                                                       final HandlerInterceptor adapter) {
        if (adapter instanceof AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter) {
            return new InMemoryThrottledSubmissionCleaner(adapter);
        }
        return () -> {
        };
    }
}
