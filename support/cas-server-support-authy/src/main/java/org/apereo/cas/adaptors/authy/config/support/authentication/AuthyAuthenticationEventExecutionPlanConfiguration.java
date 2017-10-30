package org.apereo.cas.adaptors.authy.config.support.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.authy.AuthyAuthenticationHandler;
import org.apereo.cas.adaptors.authy.AuthyClientInstance;
import org.apereo.cas.adaptors.authy.AuthyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationRegistrationWebflowAction;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.AuthyMultifactorProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;

import org.apereo.cas.services.ServicesManager;
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
    private ServicesManager servicesManager;

    @RefreshScope
    @Bean
    public AuthyClientInstance authyClientInstance() {
        final AuthyMultifactorProperties authy = casProperties.getAuthn().getMfa().getAuthy();
        if (StringUtils.isBlank(authy.getApiKey())) {
            throw new IllegalArgumentException("Authy API key must be defined");
        }
        return new AuthyClientInstance(authy.getApiKey(), authy.getApiUrl(), 
                authy.getMailAttribute(), authy.getPhoneAttribute(),
                authy.getCountryCode());
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler authyAuthenticationHandler() {
        try {
            final AuthyMultifactorProperties authy = casProperties.getAuthn().getMfa().getAuthy();
            final boolean forceVerification = authy.isForceVerification();
            return new AuthyAuthenticationHandler(authy.getName(), servicesManager, authyPrincipalFactory(), authyClientInstance(), forceVerification);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @ConditionalOnMissingBean(name = "authyPrincipalFactory")
    @Bean
    public PrincipalFactory authyPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider authyAuthenticatorAuthenticationProvider() {
        final AuthyMultifactorAuthenticationProvider p = new AuthyMultifactorAuthenticationProvider();
        p.setBypassEvaluator(authyBypassEvaluator());
        p.setGlobalFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        p.setOrder(casProperties.getAuthn().getMfa().getAuthy().getRank());
        p.setId(casProperties.getAuthn().getMfa().getAuthy().getId());
        return p;
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass authyBypassEvaluator() {
        return MultifactorAuthenticationUtils.newMultifactorAuthenticationProviderBypass(casProperties.getAuthn().getMfa().getAuthy().getBypass());
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator authyAuthenticationMetaDataPopulator() {
        return new AuthenticationContextAttributeMetaDataPopulator(
                casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
                authyAuthenticationHandler(),
                authyAuthenticatorAuthenticationProvider()
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
            plan.registerMetadataPopulator(authyAuthenticationMetaDataPopulator());
        };
    }
}
