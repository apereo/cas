package org.apereo.cas.oidc.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.validator.authorization.OidcAuthorizationCodeResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.oidc.validator.authorization.OidcCodeIdTokenAndTokenResponseTypeAuthorizationRequestValidator;;
import org.apereo.cas.oidc.validator.authorization.OidcCodeIdTokenResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.oidc.validator.authorization.OidcCodeTokenResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.oidc.validator.authorization.OidcIdTokenAndTokenResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.oidc.validator.authorization.OidcIdTokenResponseTypeAuthorizationRequestValidator;;
import org.apereo.cas.oidc.validator.authorization.OidcProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link OidcRequestValidatorsConfiguration}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
@Configuration(value = "oidcRequestValidatorsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OidcRequestValidatorsConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory> webApplicationServiceFactory;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @ConditionalOnMissingBean(name = "oidcProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oidcProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator() {
        return new OidcProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(), registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "oidcAuthorizationCodeResponseTypeAuthorizationRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oidcAuthorizationCodeResponseTypeAuthorizationRequestValidator() {
        return new OidcAuthorizationCodeResponseTypeAuthorizationRequestValidator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(), registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "oidcIdTokenAndTokenResponseTypeAuthorizationRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oidcIdTokenAndTokenResponseTypeAuthorizationRequestValidator() {
        return new OidcIdTokenAndTokenResponseTypeAuthorizationRequestValidator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(), registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "oidcIdTokenResponseTypeAuthorizationRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oidcIdTokenResponseTypeAuthorizationRequestValidator() {
        return new OidcIdTokenResponseTypeAuthorizationRequestValidator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(), registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "oidcCodeTokenResponseTypeAuthorizationRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oidcCodeTokenResponseTypeAuthorizationRequestValidator() {
        return new OidcCodeTokenResponseTypeAuthorizationRequestValidator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(), registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "oidcCodeIdTokenResponseTypeAuthorizationRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oidcCodeIdTokenResponseTypeAuthorizationRequestValidator() {
        return new OidcCodeIdTokenResponseTypeAuthorizationRequestValidator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(), registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "oidcCodeIdTokenAndTokenResponseTypeAuthorizationRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oidcCodeIdTokenAndTokenResponseTypeAuthorizationRequestValidator() {
        return new OidcCodeIdTokenAndTokenResponseTypeAuthorizationRequestValidator(servicesManager.getObject(),
            webApplicationServiceFactory.getObject(), registeredServiceAccessStrategyEnforcer.getObject());
    }

    @ConditionalOnMissingBean(name = "oidcAuthorizationRequestValidators")
    @Bean
    @RefreshScope
    @Autowired
    public Set<OAuth20AuthorizationRequestValidator> oidcAuthorizationRequestValidators() {
        val validators = new LinkedHashSet<OAuth20AuthorizationRequestValidator>(7);
        validators.add(oidcProofKeyCodeExchangeResponseTypeAuthorizationRequestValidator());
        validators.add(oidcAuthorizationCodeResponseTypeAuthorizationRequestValidator());
        validators.add(oidcCodeTokenResponseTypeAuthorizationRequestValidator());
        validators.add(oidcCodeIdTokenResponseTypeAuthorizationRequestValidator());
        validators.add(oidcCodeIdTokenAndTokenResponseTypeAuthorizationRequestValidator());
        validators.add(oidcIdTokenAndTokenResponseTypeAuthorizationRequestValidator());
        validators.add(oidcIdTokenResponseTypeAuthorizationRequestValidator());
        return validators;
    }
}
