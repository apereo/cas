package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMfaThrottledSubmissionHandlerInterceptorAdapter;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.throttle.ThrottledRequestResponseHandler;
import org.apereo.cas.web.support.ThrottledSubmissionHandlerInterceptor;


import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This is {@link CasSimpleMultifactorAuthenticationThrottlingConfiguration}.
 *
 * @author Fotis Memis
 * @since 6.3.0
 */

@Configuration("CasSimpleMultifactorAuthenticationThrottlingConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(name = "org.apereo.cas.config.CasThrottlingConfiguration")
public class CasSimpleMultifactorAuthenticationThrottlingConfiguration implements WebMvcConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("throttlerResendTokenMap")
    private ConcurrentMap<String, ZonedDateTime> userMap;

    @RefreshScope
    @ConditionalOnMissingBean(name = "throttlerResendTokenMap")
    @Bean
    public ConcurrentMap throttlerResendTokenMap() {
        return new ConcurrentHashMap<String, ZonedDateTime>();
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "simpleMfaAuthenticationThrottle")
    @Bean
    public ThrottledSubmissionHandlerInterceptor simpleMfaAuthenticationThrottle(@Qualifier("throttledRequestResponseHandler") final ThrottledRequestResponseHandler responseHandler) {
        val mfaSimpleProperties = casProperties.getAuthn().getMfa().getSimple();
        return new CasSimpleMfaThrottledSubmissionHandlerInterceptorAdapter(userMap, mfaSimpleProperties, responseHandler);
    }


    @Bean
    @DependsOn("authenticationThrottlingExecutionPlan")
    public AuthenticationThrottlingExecutionPlan addSimpleMfaHandlerInterceptorToThrottlingPlan(
        @Qualifier("authenticationThrottlingExecutionPlan") final AuthenticationThrottlingExecutionPlan plan,
        @Qualifier("simpleMfaAuthenticationThrottle") final ThrottledSubmissionHandlerInterceptor interceptor) {
        plan.registerAuthenticationThrottleInterceptor(interceptor);
        return plan;
    }

}
