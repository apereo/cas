package org.apereo.cas.otp.config;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.token.CachingOneTimeTokenRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.otp.web.flow.OneTimeTokenAuthenticationWebflowAction;
import org.apereo.cas.otp.web.flow.OneTimeTokenAuthenticationWebflowEventResolver;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.execution.Action;

import java.time.Duration;
import java.util.Collection;

/**
 * This is {@link OneTimeTokenAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("oneTimeTokenAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class OneTimeTokenAuthenticationConfiguration {
    private static final int EXPIRE_TOKENS_IN_SECONDS = 30;

    private static final int INITIAL_CACHE_SIZE = 50;

    private static final long MAX_CACHE_SIZE = 1_000_000;
    
    @Autowired
    @Qualifier("casWebflowConfigurationContext")
    private ObjectProvider<CasWebflowEventResolutionConfigurationContext> casWebflowConfigurationContext;
    
    @Bean
    @RefreshScope
    public CasWebflowEventResolver oneTimeTokenAuthenticationWebflowEventResolver() {
        return new OneTimeTokenAuthenticationWebflowEventResolver(casWebflowConfigurationContext.getObject());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_OTP_AUTHENTICATION_ACTION)
    public Action oneTimeTokenAuthenticationWebflowAction() {
        return new OneTimeTokenAuthenticationWebflowAction(oneTimeTokenAuthenticationWebflowEventResolver());
    }
    
    @ConditionalOnMissingBean(name = "oneTimeTokenAuthenticatorTokenRepository")
    @Bean
    @RefreshScope
    public OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository() {
        final Cache<String, Collection<OneTimeToken>> storage = Caffeine.newBuilder()
            .initialCapacity(INITIAL_CACHE_SIZE)
            .maximumSize(MAX_CACHE_SIZE)
            .recordStats()
            .expireAfterWrite(Duration.ofSeconds(EXPIRE_TOKENS_IN_SECONDS))
            .build();
        return new CachingOneTimeTokenRepository(storage);
    }
}

