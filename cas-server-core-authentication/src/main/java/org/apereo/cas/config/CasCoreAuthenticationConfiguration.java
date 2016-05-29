package org.apereo.cas.config;

import org.apereo.cas.authentication.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AllAuthenticationPolicy;
import org.apereo.cas.authentication.AnyAuthenticationPolicy;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.authentication.CacheCredentialsMetaDataPopulator;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.RequiredHandlerAuthenticationPolicy;
import org.apereo.cas.web.flow.AuthenticationExceptionHandler;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * This is {@link CasCoreAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreAuthenticationConfiguration")
public class CasCoreAuthenticationConfiguration {
    
    @Bean
    public AuthenticationExceptionHandler authenticationExceptionHandler() {
        return new AuthenticationExceptionHandler();
    }
    
    @RefreshScope
    @Bean
    public AuthenticationPolicy requiredHandlerAuthenticationPolicy() {
        return new RequiredHandlerAuthenticationPolicy();
    }

    @Bean
    public AuthenticationPolicy anyAuthenticationPolicy() {
        return new AnyAuthenticationPolicy();
    }

    @Bean
    public ContextualAuthenticationPolicyFactory acceptAnyAuthenticationPolicyFactory() {
        return new AcceptAnyAuthenticationPolicyFactory();
    }
    
    @Bean
    public AuthenticationHandler acceptUsersAuthenticationHandler() {
        return new AcceptUsersAuthenticationHandler();
    }

    @Bean
    public AuthenticationPolicy allAuthenticationPolicy() {
        return new AllAuthenticationPolicy();
    }

    @RefreshScope
    @Bean
    public AuthenticationContextValidator authenticationContextValidator() {
        return new AuthenticationContextValidator();
    }

    @Bean
    public AuthenticationMetaDataPopulator cacheCredentialsMetaDataPopulator() {
        return new CacheCredentialsMetaDataPopulator();
    }
}
