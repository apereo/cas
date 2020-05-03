package org.apereo.cas.config.support.authentication;

import org.apereo.cas.adaptors.u2f.U2FAuthenticationHandler;
import org.apereo.cas.adaptors.u2f.U2FMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.u2f.U2FTokenCredential;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
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

import com.yubico.u2f.U2F;
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
 * This is {@link U2FAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("u2fAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FAuthenticationEventExecutionPlanConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("u2fService")
    private ObjectProvider<U2F> u2fService;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private ObjectProvider<U2FDeviceRepository> u2fDeviceRepository;

    @Autowired
    @Qualifier("u2fBypassEvaluator")
    private ObjectProvider<MultifactorAuthenticationProviderBypassEvaluator> u2fBypassEvaluator;

    @Autowired
    @Qualifier("failureModeEvaluator")
    private ObjectProvider<MultifactorAuthenticationFailureModeEvaluator> failureModeEvaluator;

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "u2fAuthenticationMetaDataPopulator")
    public AuthenticationMetaDataPopulator u2fAuthenticationMetaDataPopulator() {
        val authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        return new AuthenticationContextAttributeMetaDataPopulator(
            authenticationContextAttribute,
            u2fAuthenticationHandler(),
            u2fMultifactorAuthenticationProvider().getId()
        );
    }

    @ConditionalOnMissingBean(name = "u2fPrincipalFactory")
    @Bean
    public PrincipalFactory u2fPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "u2fAuthenticationHandler")
    @Bean
    @RefreshScope
    public AuthenticationHandler u2fAuthenticationHandler() {
        val u2f = this.casProperties.getAuthn().getMfa().getU2f();
        return new U2FAuthenticationHandler(u2f.getName(), servicesManager.getObject(),
            u2fPrincipalFactory(), u2fDeviceRepository.getObject(), u2fService.getObject(),
            u2f.getOrder());
    }

    @ConditionalOnMissingBean(name = "u2fMultifactorAuthenticationProvider")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider u2fMultifactorAuthenticationProvider() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val p = new U2FMultifactorAuthenticationProvider();
        p.setBypassEvaluator(u2fBypassEvaluator.getObject());
        p.setFailureMode(u2f.getFailureMode());
        p.setFailureModeEvaluator(failureModeEvaluator.getObject());
        p.setOrder(u2f.getRank());
        p.setId(u2f.getId());
        return p;
    }

    @ConditionalOnMissingBean(name = "u2fAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public AuthenticationEventExecutionPlanConfigurer u2fAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandler(u2fAuthenticationHandler());
            plan.registerAuthenticationMetadataPopulator(u2fAuthenticationMetaDataPopulator());
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(U2FTokenCredential.class));
        };
    }
}
