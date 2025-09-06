package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.metadata.MultifactorAuthenticationProviderMetadataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.inwebo.InweboMultifactorAuthenticationProvider;
import org.apereo.cas.support.inwebo.authentication.InweboAuthenticationDeviceMetadataPopulator;
import org.apereo.cas.support.inwebo.authentication.InweboAuthenticationHandler;
import org.apereo.cas.support.inwebo.authentication.InweboCredential;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * The Inwebo MFA authentication configuration.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "inwebo")
@Configuration(value = "InweboAuthenticationConfiguration", proxyBeanMethods = false)
class InweboAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "inweboPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory inweboPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Configuration(value = "InweboAuthenticationProviderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class InweboAuthenticationProviderConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "inweboMultifactorAuthenticationProvider")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationProvider inweboMultifactorAuthenticationProvider(
            final CasConfigurationProperties casProperties,
            @Qualifier("inweboBypassEvaluator")
            final MultifactorAuthenticationProviderBypassEvaluator inweboBypassEvaluator,
            @Qualifier("failureModeEvaluator")
            final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator) {
            val inwebo = casProperties.getAuthn().getMfa().getInwebo();
            val p = new InweboMultifactorAuthenticationProvider();
            p.setBypassEvaluator(inweboBypassEvaluator);
            p.setFailureMode(inwebo.getFailureMode());
            p.setFailureModeEvaluator(failureModeEvaluator);
            p.setOrder(inwebo.getRank());
            p.setId(inwebo.getId());
            return p;
        }
    }

    @Configuration(value = "InweboAuthenticationHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class InweboAuthenticationHandlerConfiguration {
        @ConditionalOnMissingBean(name = "inweboAuthenticationHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationHandler inweboAuthenticationHandler(
            @Qualifier("inweboMultifactorAuthenticationProvider")
            final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider,
            final CasConfigurationProperties casProperties,
            @Qualifier("inweboPrincipalFactory")
            final PrincipalFactory inweboPrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("inweboService")
            final InweboService inweboService) {
            return new InweboAuthenticationHandler(
                inweboPrincipalFactory, casProperties.getAuthn().getMfa().getInwebo(),
                inweboService, multifactorAuthenticationProvider);
        }

    }

    @Configuration(value = "InweboAuthenticationMetadataConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class InweboAuthenticationMetadataConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "inweboMultifactorProviderAuthenticationMetadataPopulator")
        public AuthenticationMetaDataPopulator inweboMultifactorProviderAuthenticationMetadataPopulator(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            @Qualifier("inweboMultifactorAuthenticationProvider")
            final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
            val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
            return new MultifactorAuthenticationProviderMetadataPopulator(authenticationContextAttribute,
                multifactorAuthenticationProvider, servicesManager);
        }

        @Bean
        @ConditionalOnMissingBean(name = "inweboAuthenticationMetaDataPopulator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationMetaDataPopulator inweboAuthenticationMetaDataPopulator(
            final CasConfigurationProperties casProperties,
            @Qualifier("inweboAuthenticationHandler")
            final AuthenticationHandler inweboAuthenticationHandler,
            @Qualifier("inweboMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider inweboMultifactorAuthenticationProvider) {
            return new AuthenticationContextAttributeMetaDataPopulator(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(), inweboAuthenticationHandler,
                inweboMultifactorAuthenticationProvider.getId());
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "inweboAuthenticationDeviceMetadataPopulator")
        public AuthenticationMetaDataPopulator inweboAuthenticationDeviceMetadataPopulator() {
            return new InweboAuthenticationDeviceMetadataPopulator();
        }
    }


    @Configuration(value = "InweboAuthenticationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class InweboAuthenticationPlanConfiguration {
        @ConditionalOnMissingBean(name = "inweboAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer inweboAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("inweboMultifactorProviderAuthenticationMetadataPopulator")
            final AuthenticationMetaDataPopulator inweboMultifactorProviderAuthenticationMetadataPopulator,
            @Qualifier("inweboAuthenticationHandler")
            final AuthenticationHandler inweboAuthenticationHandler,
            @Qualifier("inweboAuthenticationMetaDataPopulator")
            final AuthenticationMetaDataPopulator inweboAuthenticationMetaDataPopulator,
            @Qualifier("inweboAuthenticationDeviceMetadataPopulator")
            final AuthenticationMetaDataPopulator inweboAuthenticationDeviceMetadataPopulator) {
            return plan -> {
                plan.registerAuthenticationHandler(inweboAuthenticationHandler);
                plan.registerAuthenticationMetadataPopulators(
                    CollectionUtils.wrapList(inweboAuthenticationMetaDataPopulator,
                        inweboMultifactorProviderAuthenticationMetadataPopulator,
                        inweboAuthenticationDeviceMetadataPopulator));
                plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(InweboCredential.class));
            };
        }
    }
}
