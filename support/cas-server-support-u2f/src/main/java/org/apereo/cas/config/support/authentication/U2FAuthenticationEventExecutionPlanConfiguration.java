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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link U2FAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "u2fAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
public class U2FAuthenticationEventExecutionPlanConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "u2fAuthenticationMetaDataPopulator")
    @Autowired
    public AuthenticationMetaDataPopulator u2fAuthenticationMetaDataPopulator(final CasConfigurationProperties casProperties,
                                                                              @Qualifier("u2fAuthenticationHandler")
                                                                              final AuthenticationHandler u2fAuthenticationHandler,
                                                                              @Qualifier("u2fMultifactorAuthenticationProvider")
                                                                              final MultifactorAuthenticationProvider u2fMultifactorAuthenticationProvider) {
        val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
        return new AuthenticationContextAttributeMetaDataPopulator(authenticationContextAttribute, u2fAuthenticationHandler, u2fMultifactorAuthenticationProvider.getId());
    }

    @ConditionalOnMissingBean(name = "u2fPrincipalFactory")
    @Bean
    public PrincipalFactory u2fPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "u2fAuthenticationHandler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuthenticationHandler u2fAuthenticationHandler(final CasConfigurationProperties casProperties,
                                                          @Qualifier("u2fPrincipalFactory")
                                                          final PrincipalFactory u2fPrincipalFactory,
                                                          @Qualifier("u2fService")
                                                          final U2F u2fService,
                                                          @Qualifier(ServicesManager.BEAN_NAME)
                                                          final ServicesManager servicesManager,
                                                          @Qualifier("u2fDeviceRepository")
                                                          final U2FDeviceRepository u2fDeviceRepository) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        return new U2FAuthenticationHandler(u2f.getName(), servicesManager, u2fPrincipalFactory, u2fDeviceRepository, u2fService, u2f.getOrder());
    }

    @ConditionalOnMissingBean(name = "u2fMultifactorAuthenticationProvider")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProvider u2fMultifactorAuthenticationProvider(final CasConfigurationProperties casProperties,
                                                                                  @Qualifier("u2fBypassEvaluator")
                                                                                  final MultifactorAuthenticationProviderBypassEvaluator u2fBypassEvaluator,
                                                                                  @Qualifier("failureModeEvaluator")
                                                                                  final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val p = new U2FMultifactorAuthenticationProvider();
        p.setBypassEvaluator(u2fBypassEvaluator);
        p.setFailureMode(u2f.getFailureMode());
        p.setFailureModeEvaluator(failureModeEvaluator);
        p.setOrder(u2f.getRank());
        p.setId(u2f.getId());
        return p;
    }

    @ConditionalOnMissingBean(name = "u2fAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer u2fAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("u2fAuthenticationHandler")
        final AuthenticationHandler u2fAuthenticationHandler,
        @Qualifier("u2fAuthenticationMetaDataPopulator")
        final AuthenticationMetaDataPopulator u2fAuthenticationMetaDataPopulator) {
        return plan -> {
            plan.registerAuthenticationHandler(u2fAuthenticationHandler);
            plan.registerAuthenticationMetadataPopulator(u2fAuthenticationMetaDataPopulator);
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(U2FTokenCredential.class));
        };
    }
}
