package org.apereo.cas.config;

import org.apereo.cas.adaptors.swivel.SwivelAuthenticationHandler;
import org.apereo.cas.adaptors.swivel.SwivelMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.swivel.SwivelTokenCredential;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
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
 * This is {@link SwivelAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@Configuration("swivelAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SwivelAuthenticationEventExecutionPlanConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("swivelBypassEvaluator")
    private ObjectProvider<MultifactorAuthenticationProviderBypassEvaluator> swivelBypassEvaluator;

    @Autowired
    @Qualifier("failureModeEvaluator")
    private ObjectProvider<MultifactorAuthenticationFailureModeEvaluator> failureModeEvaluator;

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator swivelAuthenticationMetaDataPopulator() {
        val authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        return new AuthenticationContextAttributeMetaDataPopulator(
            authenticationContextAttribute,
            swivelAuthenticationHandler(),
            swivelMultifactorAuthenticationProvider().getId()
        );
    }

    @ConditionalOnMissingBean(name = "swivelPrincipalFactory")
    @Bean
    public PrincipalFactory swivelPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public SwivelAuthenticationHandler swivelAuthenticationHandler() {
        val swivel = this.casProperties.getAuthn().getMfa().getSwivel();
        return new SwivelAuthenticationHandler(swivel.getName(),
            servicesManager.getIfAvailable(), swivelPrincipalFactory(), swivel);
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider swivelMultifactorAuthenticationProvider() {
        val swivel = this.casProperties.getAuthn().getMfa().getSwivel();
        val p = new SwivelMultifactorAuthenticationProvider(swivel.getSwivelUrl());
        p.setBypassEvaluator(swivelBypassEvaluator.getIfAvailable());
        p.setFailureMode(swivel.getFailureMode());
        p.setFailureModeEvaluator(failureModeEvaluator.getIfAvailable());
        p.setOrder(swivel.getRank());
        p.setId(swivel.getId());
        return p;
    }

    @ConditionalOnMissingBean(name = "swivelAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer swivelAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandler(swivelAuthenticationHandler());
            plan.registerAuthenticationMetadataPopulator(swivelAuthenticationMetaDataPopulator());
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(SwivelTokenCredential.class));
        };
    }
}
