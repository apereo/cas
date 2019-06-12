package org.apereo.cas.adaptors.authy.config.support.authentication;

import org.apereo.cas.adaptors.authy.AuthyAuthenticationHandler;
import org.apereo.cas.adaptors.authy.AuthyClientInstance;
import org.apereo.cas.adaptors.authy.AuthyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.authy.AuthyTokenCredential;
import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationRegistrationWebflowAction;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link AuthyAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("authyAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthyAuthenticationEventExecutionPlanConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("authyBypassEvaluator")
    private ObjectProvider<MultifactorAuthenticationProviderBypassEvaluator> authyBypassEvaluator;

    @Autowired
    @Qualifier("failureModeEvaluator")
    private ObjectProvider<MultifactorAuthenticationFailureModeEvaluator> failureModeEvaluator;

    @RefreshScope
    @Bean
    public AuthyClientInstance authyClientInstance() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        if (StringUtils.isBlank(authy.getApiKey())) {
            throw new IllegalArgumentException("Authy API key must be defined");
        }
        return new AuthyClientInstance(authy.getApiKey(), authy.getApiUrl(),
            authy.getMailAttribute(), authy.getPhoneAttribute(),
            authy.getCountryCode());
    }

    @ConditionalOnMissingBean(name = "authyAuthenticationHandler")
    @RefreshScope
    @Bean
    @SneakyThrows
    public AuthenticationHandler authyAuthenticationHandler() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val forceVerification = authy.isForceVerification();
        return new AuthyAuthenticationHandler(authy.getName(), servicesManager.getIfAvailable(),
            authyPrincipalFactory(), authyClientInstance(), forceVerification, authy.getOrder());
    }

    @ConditionalOnMissingBean(name = "authyPrincipalFactory")
    @Bean
    public PrincipalFactory authyPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider authyAuthenticatorMultifactorAuthenticationProvider() {
        val p = new AuthyMultifactorAuthenticationProvider();
        p.setBypassEvaluator(authyBypassEvaluator.getIfAvailable());
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        p.setFailureMode(authy.getFailureMode());
        p.setFailureModeEvaluator(failureModeEvaluator.getIfAvailable());
        p.setOrder(authy.getRank());
        p.setId(authy.getId());
        return p;
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator authyAuthenticationMetaDataPopulator() {
        return new AuthenticationContextAttributeMetaDataPopulator(
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            authyAuthenticationHandler(),
            authyAuthenticatorMultifactorAuthenticationProvider().getId()
        );
    }

    @RefreshScope
    @Bean
    public Action authyAuthenticationRegistrationWebflowAction() {
        return new AuthyAuthenticationRegistrationWebflowAction(authyClientInstance());
    }

    @ConditionalOnMissingBean(name = "authyAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer authyAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandler(authyAuthenticationHandler());
            plan.registerAuthenticationMetadataPopulator(authyAuthenticationMetaDataPopulator());
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(AuthyTokenCredential.class));
        };
    }
}
