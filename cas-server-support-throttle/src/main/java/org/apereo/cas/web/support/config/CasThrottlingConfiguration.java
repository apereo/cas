package org.apereo.cas.web.support.config;

import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.NeverThrottledSubmissionHandlerInterceptorAdapter;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * This is {@link CasThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casThrottlingConfiguration")
public class CasThrottlingConfiguration {
    
    @Bean
    @RefreshScope
    public HandlerInterceptorAdapter inMemoryIpAddressUsernameThrottle() {
        return new InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter();
    }

    @Bean
    public HandlerInterceptorAdapter inMemoryIpAddressThrottle() {
        return new InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter();
    }

    @Bean
    @RefreshScope
    public HandlerInterceptorAdapter inspektrIpAddressUsernameThrottle() {
        return new InspektrThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter();
    }

    @Bean
    public HandlerInterceptorAdapter neverThrottle() {
        return new NeverThrottledSubmissionHandlerInterceptorAdapter();
    }
}
