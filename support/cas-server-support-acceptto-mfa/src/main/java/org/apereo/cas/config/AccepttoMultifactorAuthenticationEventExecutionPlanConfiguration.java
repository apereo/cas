package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.AccepttoMultifactorAuthenticationHandler;
import org.apereo.cas.mfa.accepto.AccepttoMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.accepto.AccepttoMultifactorTokenCredential;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
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
 * This is {@link AccepttoMultifactorAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("accepttoMultifactorAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class AccepttoMultifactorAuthenticationEventExecutionPlanConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("casAccepttoMultifactorBypassEvaluator")
    private ObjectProvider<MultifactorAuthenticationProviderBypass> casAccepttoMultifactorBypassEvaluator;

    @ConditionalOnMissingBean(name = "casAccepttoMultifactorAuthenticationHandler")
    @Bean
    @RefreshScope
    public AuthenticationHandler casAccepttoMultifactorAuthenticationHandler() {
        val props = casProperties.getAuthn().getMfa().getAcceptto();
        return new AccepttoMultifactorAuthenticationHandler(props.getName(),
            servicesManager.getIfAvailable(), casAccepttoMultifactorPrincipalFactory(),
            props.getOrder());
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider casAccepttoMultifactorAuthenticationProvider() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val p = new AccepttoMultifactorAuthenticationProvider();
        p.setBypassEvaluator(casAccepttoMultifactorBypassEvaluator.getIfAvailable());
        p.setFailureMode(simple.getFailureMode());
        p.setOrder(simple.getRank());
        p.setId(simple.getId());
        return p;
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator casAccepttoMultifactorAuthenticationMetaDataPopulator() {
        return new AuthenticationContextAttributeMetaDataPopulator(
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            casAccepttoMultifactorAuthenticationHandler(),
            casAccepttoMultifactorAuthenticationProvider().getId()
        );
    }

    @ConditionalOnMissingBean(name = "casAccepttoMultifactorPrincipalFactory")
    @Bean
    public PrincipalFactory casAccepttoMultifactorPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "casAccepttoMultifactorAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer casAccepttoMultifactorAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandler(casAccepttoMultifactorAuthenticationHandler());
            plan.registerAuthenticationMetadataPopulator(casAccepttoMultifactorAuthenticationMetaDataPopulator());
            plan.registerAuthenticationHandlerResolver(
                new ByCredentialTypeAuthenticationHandlerResolver(AccepttoMultifactorTokenCredential.class));
        };
    }
}
