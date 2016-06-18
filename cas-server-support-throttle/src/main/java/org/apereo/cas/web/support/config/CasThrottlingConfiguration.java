package org.apereo.cas.web.support.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.AbstractThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionCleaner;
import org.apereo.cas.web.support.NeverThrottledSubmissionHandlerInterceptorAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * This is {@link CasThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casThrottlingConfiguration")
public class CasThrottlingConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

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
        return new NeverThrottledSubmissionHandlerInterceptorAdapter();
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

    @ConditionalOnExpression("'${authenticationThrottle}' matches 'inMemory.+'")
    @Bean
    public InMemoryThrottledSubmissionCleaner inMemoryThrottledSubmissionCleaner(@Qualifier("authenticationThrottle")
                                                                                 final HandlerInterceptor adapter) {
        return new InMemoryThrottledSubmissionCleaner(adapter);
    }
}
