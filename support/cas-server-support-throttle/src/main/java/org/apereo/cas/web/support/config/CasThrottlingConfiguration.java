package org.apereo.cas.web.support.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.AbstractThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionCleaner;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    @Bean
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle() {

        if (casProperties.getAuthn().getThrottle().getFailure().getThreshold() > 0
                && casProperties.getAuthn().getThrottle().getFailure().getRangeSeconds() > 0) {
            if (StringUtils.isNotBlank(casProperties.getAuthn().getThrottle().getUsernameParameter())) {
                return inMemoryIpAddressUsernameThrottle();
            }
            return inMemoryIpAddressThrottle();
        }
        return neverThrottle();
    }

    @Bean
    public Runnable throttleSubmissionCleaner(@Qualifier("authenticationThrottle")
                                              final ThrottledSubmissionHandlerInterceptor adapter) {
        return new InMemoryThrottledSubmissionCleaner(adapter);
    }

    private ThrottledSubmissionHandlerInterceptor inMemoryIpAddressUsernameThrottle() {
        return configureInMemoryInterceptorAdaptor(
                new InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter());
    }


    private ThrottledSubmissionHandlerInterceptor inMemoryIpAddressThrottle() {
        return configureInMemoryInterceptorAdaptor(
                new InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter());
    }


    private static ThrottledSubmissionHandlerInterceptor neverThrottle() {
        return new ThrottledSubmissionHandlerInterceptor() {
        };
    }

    private AbstractThrottledSubmissionHandlerInterceptorAdapter
    configureThrottleHandlerInterceptorAdaptor(final AbstractThrottledSubmissionHandlerInterceptorAdapter interceptorAdapter) {
        interceptorAdapter.setUsernameParameter(casProperties.getAuthn().getThrottle().getUsernameParameter());
        interceptorAdapter.setFailureThreshold(casProperties.getAuthn().getThrottle().getFailure().getThreshold());
        interceptorAdapter.setFailureRangeInSeconds(casProperties.getAuthn().getThrottle().getFailure().getRangeSeconds());
        return interceptorAdapter;
    }

    private ThrottledSubmissionHandlerInterceptor
    configureInMemoryInterceptorAdaptor(final AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter interceptorAdapter) {
        return configureThrottleHandlerInterceptorAdaptor(interceptorAdapter);
    }

}
