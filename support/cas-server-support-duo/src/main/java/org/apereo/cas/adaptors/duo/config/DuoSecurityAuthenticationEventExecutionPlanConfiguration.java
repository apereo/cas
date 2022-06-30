package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.adaptors.duo.DuoSecurityHealthIndicator;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationHandler;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProviderFactory;
import org.apereo.cas.adaptors.duo.web.DuoSecurityAdminApiEndpoint;
import org.apereo.cas.adaptors.duo.web.DuoSecurityPingEndpoint;
import org.apereo.cas.adaptors.duo.web.DuoSecurityUserAccountStatusEndpoint;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityDetermineUserAccountAction;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityPrepareWebLoginFormAction;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBean;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderFactoryBean;
import org.apereo.cas.authentication.bypass.ChainingMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.metadata.MultifactorAuthenticationProviderMetadataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * This is {@link DuoSecurityAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "duo")
@AutoConfiguration
public class DuoSecurityAuthenticationEventExecutionPlanConfiguration {

    @Configuration(value = "DuoSecurityAuthenticationEventExecutionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DuoSecurityAuthenticationEventExecutionConfiguration {
        private static BeanContainer<AuthenticationMetaDataPopulator> duoAuthenticationMetaDataPopulator(
            final ConfigurableApplicationContext applicationContext,
            final DuoSecurityAuthenticationHandler authenticationHandler,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(BeanContainer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
                    val p1 = new AuthenticationContextAttributeMetaDataPopulator(
                        casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(),
                        authenticationHandler, authenticationHandler.getMultifactorAuthenticationProvider().getObject().getId());
                    val p2 = new MultifactorAuthenticationProviderMetadataPopulator(authenticationContextAttribute,
                        authenticationHandler.getMultifactorAuthenticationProvider(),
                        applicationContext.getBean(ServicesManager.class, ServicesManager.class));
                    return BeanContainer.of(p1, p2);
                })
                .otherwise(BeanContainer::empty)
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "duoAuthenticationHandlers")
        public BeanContainer<DuoSecurityAuthenticationHandler> duoAuthenticationHandlers(
            final ConfigurableApplicationContext applicationContext,
            final List<MultifactorAuthenticationPrincipalResolver> resolvers,
            final CasConfigurationProperties casProperties,
            @Qualifier("duoPrincipalFactory")
            final PrincipalFactory duoPrincipalFactory,
            @Qualifier("duoProviderBean")
            final MultifactorAuthenticationProviderBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {

            return BeanSupplier.of(BeanContainer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    AnnotationAwareOrderComparator.sort(resolvers);
                    return BeanContainer.of(casProperties.getAuthn().getMfa().getDuo()
                        .stream()
                        .map(props -> {
                            val provider = duoProviderBean.getProvider(props.getId());
                            return new DuoSecurityAuthenticationHandler(props.getName(),
                                servicesManager, duoPrincipalFactory,
                                new DirectObjectProvider<>(provider),
                                props.getOrder(), resolvers);
                        })
                        .sorted(Comparator.comparing(DuoSecurityAuthenticationHandler::getOrder))
                        .collect(Collectors.toList()));
                })
                .otherwise(BeanContainer::empty)
                .get();
        }


        @ConditionalOnMissingBean(name = "duoSecurityAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer duoSecurityAuthenticationEventExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("duoAuthenticationHandlers")
            final BeanContainer<DuoSecurityAuthenticationHandler> duoAuthenticationHandlers) {
            return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> {
                    duoAuthenticationHandlers.toList().forEach(dh -> {
                        plan.registerAuthenticationHandler(dh);
                        val populators = duoAuthenticationMetaDataPopulator(applicationContext, dh, casProperties);
                        plan.registerAuthenticationMetadataPopulators(populators.toList());
                    });
                    plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(
                        DuoSecurityCredential.class, DuoSecurityDirectCredential.class));
                })
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "DuoSecurityAuthenticationMonitorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DuoSecurityAuthenticationMonitorConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnEnabledHealthIndicator("duoSecurityHealthIndicator")
        public HealthIndicator duoSecurityHealthIndicator(final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(HealthIndicator.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DuoSecurityHealthIndicator(applicationContext))
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "DuoSecurityAuthenticationEventExecutionPlanCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DuoSecurityAuthenticationEventExecutionPlanCoreConfiguration {
        @ConditionalOnMissingBean(name = "duoPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory duoPrincipalFactory(final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(PrincipalFactory.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(PrincipalFactoryUtils::newPrincipalFactory)
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "duoProviderFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationProviderFactoryBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderFactory(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            final List<MultifactorAuthenticationPrincipalResolver> resolvers,
            @Qualifier("httpClient")
            final HttpClient httpClient,
            @Qualifier("duoSecurityBypassEvaluator")
            final ChainingMultifactorAuthenticationProviderBypassEvaluator duoSecurityBypassEvaluator,
            @Qualifier("failureModeEvaluator")
            final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator) {
            return BeanSupplier.of(MultifactorAuthenticationProviderFactoryBean.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    AnnotationAwareOrderComparator.sort(resolvers);
                    return new DuoSecurityMultifactorAuthenticationProviderFactory(httpClient, duoSecurityBypassEvaluator,
                        failureModeEvaluator, casProperties, resolvers);
                })
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "duoProviderBean")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MultifactorAuthenticationProviderBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean(
            final CasConfigurationProperties casProperties,
            final GenericWebApplicationContext applicationContext,
            @Qualifier("duoProviderFactory")
            final MultifactorAuthenticationProviderFactoryBean<DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderFactory) {
            return new MultifactorAuthenticationProviderBean(duoProviderFactory,
                applicationContext.getDefaultListableBeanFactory(), casProperties.getAuthn().getMfa().getDuo());
        }

    }

    @Configuration(value = "DuoSecurityAuthenticationWebflowActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DuoSecurityAuthenticationWebflowActionsConfiguration {
        @ConditionalOnMissingBean(name = "duoMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer duoMultifactorWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DuoSecurityMultifactorWebflowConfigurer(flowBuilderServices,
                    loginFlowDefinitionRegistry, applicationContext, casProperties,
                    MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext)))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "duoSecurityCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer duoSecurityCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("duoMultifactorWebflowConfigurer")
            final CasWebflowConfigurer duoMultifactorWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(duoMultifactorWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_PREPARE_DUO_WEB_LOGIN_FORM)
        public Action prepareDuoWebLoginFormAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                    .supply(DuoSecurityPrepareWebLoginFormAction::new)
                    .otherwiseProxy()
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_PREPARE_DUO_WEB_LOGIN_FORM)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DETERMINE_DUO_USER_ACCOUNT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action determineDuoUserAccountAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                    .supply(DuoSecurityDetermineUserAccountAction::new)
                    .otherwiseProxy()
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_DETERMINE_DUO_USER_ACCOUNT)
                .build()
                .get();
        }
    }

    @Configuration(value = "DuoSecurityAuthenticationEventExecutionPlanWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class DuoSecurityAuthenticationEventExecutionPlanWebConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DuoSecurityPingEndpoint duoPingEndpoint(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new DuoSecurityPingEndpoint(casProperties, applicationContext);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DuoSecurityUserAccountStatusEndpoint duoAccountStatusEndpoint(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new DuoSecurityUserAccountStatusEndpoint(casProperties, applicationContext);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DuoSecurityAdminApiEndpoint duoAdminApiEndpoint(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new DuoSecurityAdminApiEndpoint(casProperties, applicationContext);
        }
    }
}
