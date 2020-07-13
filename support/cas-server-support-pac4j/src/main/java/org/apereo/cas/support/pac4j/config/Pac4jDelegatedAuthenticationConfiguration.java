package org.apereo.cas.support.pac4j.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.DelegatedAuthenticationServiceTicketValidationAuthorizer;
import org.apereo.cas.validation.RegisteredServiceDelegatedAuthenticationPolicyAuditableEnforcer;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizer;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizerConfigurer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.scribejava.core.model.OAuth1RequestToken;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link Pac4jDelegatedAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("pac4jDelegatedAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class Pac4jDelegatedAuthenticationConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer")
    public AuditableExecution registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer() {
        return new RegisteredServiceDelegatedAuthenticationPolicyAuditableEnforcer();
    }

    @Bean
    @ConditionalOnMissingBean(name = "pac4jJacksonModule")
    public Module pac4jJacksonModule() {
        val module = new SimpleModule();
        module.setMixInAnnotation(OAuth1RequestToken.class, AbstractOAuth1RequestTokenMixin.class);
        return module;
    }

    @Bean
    @RefreshScope
    public ServiceTicketValidationAuthorizer pac4jServiceTicketValidationAuthorizer() {
        return new DelegatedAuthenticationServiceTicketValidationAuthorizer(servicesManager.getObject(),
            registeredServiceDelegatedAuthenticationPolicyAuditableEnforcer());
    }

    @Bean
    @RefreshScope
    public ServiceTicketValidationAuthorizerConfigurer pac4jServiceTicketValidationAuthorizerConfigurer() {
        return plan -> plan.registerAuthorizer(pac4jServiceTicketValidationAuthorizer());
    }


    /**
     * The type Oauth1 request token mixin.
     */
    private abstract static class AbstractOAuth1RequestTokenMixin extends OAuth1RequestToken {
        private static final long serialVersionUID = -7839084408338396531L;

        @JsonCreator
        AbstractOAuth1RequestTokenMixin(@JsonProperty("token") final String token,
                                        @JsonProperty("tokenSecret") final String tokenSecret,
                                        @JsonProperty("oauthCallbackConfirmed") final boolean oauthCallbackConfirmed,
                                        @JsonProperty("rawResponse") final String rawResponse) {
            super(token, tokenSecret, oauthCallbackConfirmed, rawResponse);
        }

        @JsonIgnore
        @Override
        public abstract boolean isEmpty();
    }
}
