package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.swivel.SwivelAuthenticationHandler;
import org.apereo.cas.adaptors.swivel.SwivelTokenCredential;
import org.apereo.cas.adaptors.swivel.SwivelMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.SwivelMultifactorProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.ServicesManager;
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
@Slf4j
public class SwivelAuthenticationEventExecutionPlanConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator swivelAuthenticationMetaDataPopulator() {
        final String authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        return new AuthenticationContextAttributeMetaDataPopulator(
                authenticationContextAttribute,
                swivelAuthenticationHandler(),
                swivelAuthenticationProvider().getId()
        );
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass swivelBypassEvaluator() {
        return MultifactorAuthenticationUtils.newMultifactorAuthenticationProviderBypass(casProperties.getAuthn().getMfa().getSwivel().getBypass());
    }

    @ConditionalOnMissingBean(name = "swivelPrincipalFactory")
    @Bean
    public PrincipalFactory swivelPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public SwivelAuthenticationHandler swivelAuthenticationHandler() {
        final SwivelMultifactorProperties swivel = this.casProperties.getAuthn().getMfa().getSwivel();
        return new SwivelAuthenticationHandler(swivel.getName(),
                servicesManager, swivelPrincipalFactory(), swivel);
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider swivelAuthenticationProvider() {
        final SwivelMultifactorProperties swivel = this.casProperties.getAuthn().getMfa().getSwivel();
        final SwivelMultifactorAuthenticationProvider p = new SwivelMultifactorAuthenticationProvider(swivel.getSwivelUrl());
        p.setBypassEvaluator(swivelBypassEvaluator());
        p.setFailureMode(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        p.setOrder(swivel.getRank());
        p.setId(swivel.getId());
        return p;
    }

    @ConditionalOnMissingBean(name = "swivelAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer swivelAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandler(swivelAuthenticationHandler());
            plan.registerMetadataPopulator(swivelAuthenticationMetaDataPopulator());
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(SwivelTokenCredential.class));
        };
    }
}
