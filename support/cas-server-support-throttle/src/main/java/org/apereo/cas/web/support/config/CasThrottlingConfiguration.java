package org.apereo.cas.web.support.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.throttle.ThrottleProperties;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter;
import org.apereo.cas.web.support.InMemoryThrottledSubmissionCleaner;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * This is {@link CasThrottlingConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casThrottlingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasCoreUtilConfiguration.class)
public class CasThrottlingConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasThrottlingConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @ConditionalOnMissingBean(name = "authenticationThrottle")
    @Bean
    public ThrottledSubmissionHandlerInterceptor authenticationThrottle() {
        final ThrottleProperties throttle = casProperties.getAuthn().getThrottle();
        if (throttle.getFailure().getThreshold() > 0
                && throttle.getFailure().getRangeSeconds() > 0) {
            if (StringUtils.isNotBlank(throttle.getUsernameParameter())) {
                return new InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter(throttle.getFailure().getThreshold(),
                        throttle.getFailure().getRangeSeconds(), throttle.getUsernameParameter());
            }
            return new InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter(throttle.getFailure().getThreshold(),
                    throttle.getFailure().getRangeSeconds(), throttle.getUsernameParameter());
        }
        return neverThrottle();
    }

    @Lazy
    @Bean
    public Runnable throttleSubmissionCleaner(@Qualifier("authenticationThrottle") final ThrottledSubmissionHandlerInterceptor adapter) {
        return new InMemoryThrottledSubmissionCleaner(adapter);
    }

    private static ThrottledSubmissionHandlerInterceptor neverThrottle() {
        return () -> LOGGER.debug("Throttling is turned off. No cleanup will take place");
    }
}
