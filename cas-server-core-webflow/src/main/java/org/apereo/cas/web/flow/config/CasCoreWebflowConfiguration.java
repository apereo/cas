package org.apereo.cas.web.flow.config;

import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.authentication.FirstMultifactorAuthenticationProviderSelector;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.InitialAuthenticationAttemptWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.PrincipalAttributeAuthenticationPolicyWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.RankedAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.RegisteredServiceAuthenticationPolicyWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.RegisteredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.RequestParameterAuthenticationPolicyWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.SelectiveAuthenticationProviderWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.ServiceTicketRequestWebflowEventResolver;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreWebflowConfiguration")
public class CasCoreWebflowConfiguration {
    
    @Bean
    @RefreshScope
    public CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver() {
        return new PrincipalAttributeAuthenticationPolicyWebflowEventResolver();
    }
    
    @Bean
    public MultifactorAuthenticationProviderSelector firstMultifactorAuthenticationProviderSelector() {
        return new FirstMultifactorAuthenticationProviderSelector();
    }
    
    @Bean
    public CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver() {
        return new InitialAuthenticationAttemptWebflowEventResolver();
    }
    
    @Bean
    public CasWebflowEventResolver serviceTicketRequestWebflowEventResolver() {
        return new ServiceTicketRequestWebflowEventResolver();
    }

    @Bean
    public CasWebflowEventResolver selectiveAuthenticationProviderWebflowEventResolver() {
        return new SelectiveAuthenticationProviderWebflowEventResolver();
    }

    @Bean
    @RefreshScope
    public CasWebflowEventResolver requestParameterAuthenticationPolicyWebflowEventResolver() {
        return new RequestParameterAuthenticationPolicyWebflowEventResolver();
    }

    @Bean
    @RefreshScope
    public CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver() {
        return new RegisteredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver();
    }

    @Bean
    public CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver() {
        return new RegisteredServiceAuthenticationPolicyWebflowEventResolver();
    }

    @Bean
    @RefreshScope
    public CasWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver() {
        return new RankedAuthenticationProviderWebflowEventResolver();
    }
}
