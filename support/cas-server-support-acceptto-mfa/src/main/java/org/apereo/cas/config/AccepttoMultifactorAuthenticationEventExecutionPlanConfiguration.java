package org.apereo.cas.config;

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
import org.apereo.cas.mfa.accepto.AccepttoMultifactorAuthenticationHandler;
import org.apereo.cas.mfa.accepto.AccepttoMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.accepto.AccepttoMultifactorTokenCredential;
import org.apereo.cas.services.ServicesManager;

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
 * This is {@link AccepttoMultifactorAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "accepttoMultifactorAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
public class AccepttoMultifactorAuthenticationEventExecutionPlanConfiguration {

    @Configuration(value = "AccepttoMultifactorAuthenticationEventExecutionPlanHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationEventExecutionPlanHandlerConfiguration {
        @ConditionalOnMissingBean(name = "casAccepttoMultifactorAuthenticationHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public AuthenticationHandler casAccepttoMultifactorAuthenticationHandler(
            final CasConfigurationProperties casProperties,
            @Qualifier("casAccepttoMultifactorPrincipalFactory")
            final PrincipalFactory casAccepttoMultifactorPrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val props = casProperties.getAuthn().getMfa().getAcceptto();
            return new AccepttoMultifactorAuthenticationHandler(servicesManager, casAccepttoMultifactorPrincipalFactory, props);
        }
    }


    @Configuration(value = "AccepttoMultifactorAuthenticationEventExecutionPlanProviderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationEventExecutionPlanProviderConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "casAccepttoMultifactorAuthenticationProvider")
        @Autowired
        public MultifactorAuthenticationProvider casAccepttoMultifactorAuthenticationProvider(
            final CasConfigurationProperties casProperties,
            @Qualifier("casAccepttoMultifactorBypassEvaluator")
            final MultifactorAuthenticationProviderBypassEvaluator casAccepttoMultifactorBypassEvaluator,
            @Qualifier("failureModeEvaluator")
            final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator) {
            val simple = casProperties.getAuthn().getMfa().getAcceptto();
            val p = new AccepttoMultifactorAuthenticationProvider();
            p.setBypassEvaluator(casAccepttoMultifactorBypassEvaluator);
            p.setFailureMode(simple.getFailureMode());
            p.setFailureModeEvaluator(failureModeEvaluator);
            p.setOrder(simple.getRank());
            p.setId(simple.getId());
            return p;
        }
    }

    @Configuration(value = "AccepttoMultifactorAuthenticationEventExecutionPlanMetadataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationEventExecutionPlanMetadataConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "casAccepttoMultifactorAuthenticationMetaDataPopulator")
        @Autowired
        public AuthenticationMetaDataPopulator casAccepttoMultifactorAuthenticationMetaDataPopulator(
            final CasConfigurationProperties casProperties,
            @Qualifier("casAccepttoMultifactorAuthenticationHandler")
            final AuthenticationHandler casAccepttoMultifactorAuthenticationHandler,
            @Qualifier("casAccepttoMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider casAccepttoMultifactorAuthenticationProvider) {
            return new AuthenticationContextAttributeMetaDataPopulator(
                casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(),
                casAccepttoMultifactorAuthenticationHandler, casAccepttoMultifactorAuthenticationProvider.getId());
        }

    }

    @Configuration(value = "AccepttoMultifactorAuthenticationEventExecutionPlanPrincipalConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationEventExecutionPlanPrincipalConfiguration {
        @ConditionalOnMissingBean(name = "casAccepttoMultifactorPrincipalFactory")
        @Bean
        public PrincipalFactory casAccepttoMultifactorPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }

    }
    
    @Configuration(value = "AccepttoMultifactorAuthenticationEventExecutionPlanBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class AccepttoMultifactorAuthenticationEventExecutionPlanBaseConfiguration {
        @ConditionalOnMissingBean(name = "casAccepttoMultifactorAuthenticationEventExecutionPlanConfigurer")
        @Bean
        public AuthenticationEventExecutionPlanConfigurer casAccepttoMultifactorAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("casAccepttoMultifactorAuthenticationHandler")
            final AuthenticationHandler casAccepttoMultifactorAuthenticationHandler,
            @Qualifier("casAccepttoMultifactorAuthenticationMetaDataPopulator")
            final AuthenticationMetaDataPopulator casAccepttoMultifactorAuthenticationMetaDataPopulator) {
            return plan -> {
                plan.registerAuthenticationHandler(casAccepttoMultifactorAuthenticationHandler);
                plan.registerAuthenticationMetadataPopulator(casAccepttoMultifactorAuthenticationMetaDataPopulator);
                plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(AccepttoMultifactorTokenCredential.class));
            };
        }
    }
}
