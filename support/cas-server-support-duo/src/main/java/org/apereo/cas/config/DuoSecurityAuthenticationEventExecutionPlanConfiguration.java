package org.apereo.cas.config;

import org.apereo.cas.adaptors.duo.DuoSecurityHealthIndicator;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.authn.DefaultDuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationHandler;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityDirectCredential;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationDeviceManager;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.UniversalPromptDuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.web.DuoSecurityAdminApiEndpoint;
import org.apereo.cas.adaptors.duo.web.DuoSecurityPingEndpoint;
import org.apereo.cas.adaptors.duo.web.DuoSecurityUserAccountStatusEndpoint;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityMultifactorWebflowConfigurer;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityDetermineUserAccountAction;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.bypass.ChainingMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.metadata.MultifactorAuthenticationProviderMetadataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ImmutableInMemoryServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import com.duosecurity.Client;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
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
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication,
    module = "duo")
@Configuration(value = "DuoSecurityAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
class DuoSecurityAuthenticationEventExecutionPlanConfiguration {

    private static final int WEBFLOW_CONFIGURER_ORDER = 0;

    @Configuration(value = "DuoSecurityAuthenticationEventExecutionConfiguration",
        proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DuoSecurityAuthenticationEventExecutionConfiguration {
        private static BeanContainer<AuthenticationMetaDataPopulator> duoAuthenticationMetaDataPopulator(
            final ConfigurableApplicationContext applicationContext,
            final MultifactorAuthenticationHandler authenticationHandler,
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
        @DependsOn("duoMultifactorAuthenticationProviders")
        public BeanContainer<MultifactorAuthenticationHandler> duoAuthenticationHandlers(
            final ConfigurableApplicationContext applicationContext,
            final List<MultifactorAuthenticationPrincipalResolver> resolvers,
            final CasConfigurationProperties casProperties,
            @Qualifier("duoPrincipalFactory")
            final PrincipalFactory duoPrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {

            return BeanSupplier.of(BeanContainer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    AnnotationAwareOrderComparator.sort(resolvers);
                    return BeanContainer.of(casProperties.getAuthn().getMfa().getDuo()
                        .stream()
                        .map(props -> {
                            val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(props.getId(), applicationContext)
                                .map(DuoSecurityMultifactorAuthenticationProvider.class::cast)
                                .orElseThrow(() -> new IllegalArgumentException("Unable to locate multifactor authentication provider by id " + props.getId()));
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
            final BeanContainer<MultifactorAuthenticationHandler> duoAuthenticationHandlers) {
            return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> {
                    duoAuthenticationHandlers.toList().forEach(dh -> {
                        plan.registerAuthenticationHandler(dh);
                        val populators = duoAuthenticationMetaDataPopulator(applicationContext, dh, casProperties);
                        plan.registerAuthenticationMetadataPopulators(populators.toList());
                    });
                    plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(DuoSecurityDirectCredential.class));
                })
                .otherwiseProxy()
                .get();
        }

    }

    @Configuration(value = "DuoSecurityAuthenticationMonitorConfiguration",
        proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DuoSecurityAuthenticationMonitorConfiguration {
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

    @Configuration(value = "DuoSecurityAuthenticationEventExecutionPlanCoreConfiguration",
        proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Slf4j
    static class DuoSecurityAuthenticationEventExecutionPlanCoreConfiguration {
        private static final int USER_ACCOUNT_CACHE_INITIAL_SIZE = 50;

        private static final long USER_ACCOUNT_CACHE_MAX_SIZE = 1_000;

        private static final int USER_ACCOUNT_CACHE_EXPIRATION_SECONDS = 5;

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

        @ConditionalOnMissingBean(name = "duoMultifactorAuthenticationProviders")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<MultifactorAuthenticationProvider> duoMultifactorAuthenticationProviders(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolvers,
            @Qualifier(HttpClient.BEAN_NAME_HTTPCLIENT)
            final HttpClient httpClient,
            @Qualifier("duoSecurityBypassEvaluator")
            final ChainingMultifactorAuthenticationProviderBypassEvaluator duoSecurityBypassEvaluator,
            @Qualifier("failureModeEvaluator")
            final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator) {

            return BeanSupplier.of(BeanContainer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    AnnotationAwareOrderComparator.sort(multifactorAuthenticationPrincipalResolvers);
                    val listOfProviders = new ArrayList<>();
                    casProperties.getAuthn().getMfa().getDuo()
                        .stream()
                        .map(duoProps -> {
                            val provider = new DefaultDuoSecurityMultifactorAuthenticationProvider();
                            provider.setFailureMode(duoProps.getFailureMode());
                            provider.setFailureModeEvaluator(failureModeEvaluator);
                            provider.setOrder(duoProps.getRank());
                            provider.setId(duoProps.getId());
                            provider.setRegistration(duoProps.getRegistration());
                            provider.setDeviceManager(new DuoSecurityMultifactorAuthenticationDeviceManager(provider));
                            val duoAuthenticationService = getDuoAuthenticationService(applicationContext,
                                multifactorAuthenticationPrincipalResolvers, httpClient, casProperties, duoProps);
                            var name = provider.getId().concat("-duoAuthenticationService");
                            ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, duoAuthenticationService, name);
                            provider.setDuoAuthenticationService(duoAuthenticationService);

                            val bypassEvaluator = duoSecurityBypassEvaluator.filterMultifactorAuthenticationProviderBypassEvaluatorsBy(duoProps.getId());
                            name = provider.getId().concat("-duoBypassEvaluator");
                            ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, bypassEvaluator, name);
                            provider.setBypassEvaluator(bypassEvaluator);

                            return provider;
                        }).forEach(provider -> {
                            val name = provider.getId().concat("-duoSecurityMfaProvider");
                            ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, provider, name);
                            listOfProviders.add(provider);
                        });
                    return BeanContainer.of(listOfProviders);
                })
                .otherwise(BeanContainer::empty)
                .get();
        }

        private static DuoSecurityAuthenticationService getDuoAuthenticationService(
            final ConfigurableApplicationContext applicationContext,
            final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolvers,
            final HttpClient httpClient,
            final CasConfigurationProperties casProperties,
            final DuoSecurityMultifactorAuthenticationProperties properties) {
            return FunctionUtils.doUnchecked(() -> {
                val cache = Caffeine.newBuilder()
                    .initialCapacity(USER_ACCOUNT_CACHE_INITIAL_SIZE)
                    .maximumSize(USER_ACCOUNT_CACHE_MAX_SIZE)
                    .expireAfterWrite(Duration.ofSeconds(USER_ACCOUNT_CACHE_EXPIRATION_SECONDS))
                    .<String, DuoSecurityUserAccount>build();

                LOGGER.trace("Activating universal prompt authentication service for duo security");
                val resolver = SpringExpressionLanguageValueResolver.getInstance();
                val duoClient = applicationContext.getBeanProvider(Client.class)
                    .getIfAvailable(Unchecked.supplier(() ->
                        new Client.Builder(
                            resolver.resolve(properties.getDuoIntegrationKey()),
                            resolver.resolve(properties.getDuoSecretKey()),
                            resolver.resolve(properties.getDuoApiHost()),
                            casProperties.getServer().getLoginUrl()).build()));
                return new UniversalPromptDuoSecurityAuthenticationService(properties, httpClient, duoClient,
                    multifactorAuthenticationPrincipalResolvers, cache);
            });
        }
    }

    @Configuration(value = "DuoSecurityAuthenticationWebflowActionsConfiguration",
        proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DuoSecurityAuthenticationWebflowActionsConfiguration {
        @ConditionalOnMissingBean(name = "duoMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer duoMultifactorWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new DuoSecurityMultifactorWebflowConfigurer(flowBuilderServices,
                        flowDefinitionRegistry, applicationContext, casProperties,
                        MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
                    cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
                    return cfg;
                })
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

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DETERMINE_DUO_USER_ACCOUNT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action determineDuoUserAccountAction(
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                    .supply(() -> new DuoSecurityDetermineUserAccountAction(casProperties,
                        servicesManager, defaultPrincipalResolver, webApplicationServiceFactory, tenantExtractor))
                    .otherwise(() -> ConsumerExecutionAction.NONE)
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_DETERMINE_DUO_USER_ACCOUNT)
                .build()
                .get();
        }


        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "duoServiceRegistryExecutionPlanConfigurer")
        public ServiceRegistryExecutionPlanConfigurer duoServiceRegistryExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return plan -> casProperties.getAuthn().getMfa().getDuo().stream()
                .filter(duo -> StringUtils.isNotBlank(duo.getRegistration().getRegistrationUrl()))
                .forEach(duo -> {
                    val serviceId = FunctionUtils.doUnchecked(() -> new URI(duo.getRegistration().getRegistrationUrl()).toURL().getHost());
                    val service = new CasRegisteredService();
                    service.setId(RandomUtils.nextInt());
                    service.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
                    service.setName(service.getClass().getSimpleName());
                    service.setDescription("Duo Security Registration URL for " + duo.getId());
                    service.setServiceId(serviceId);
                    service.markAsInternal();
                    plan.registerServiceRegistry(new ImmutableInMemoryServiceRegistry(List.of(service), applicationContext, List.of()));
                });
        }
    }

    @Configuration(value = "DuoSecurityAuthenticationEventExecutionPlanWebConfiguration",
        proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DuoSecurityAuthenticationEventExecutionPlanWebConfiguration {
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

    @Configuration(value = "SurrogateAuthenticationDuoSecurityWebflowPlanConfiguration",
        proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(SurrogateAuthenticationService.class)
    static class SurrogateAuthenticationDuoSecurityWebflowPlanConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnClass(DuoSecurityAuthenticationService.class)
        @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication,
            module = "duo")
        public CasMultifactorWebflowCustomizer surrogateDuoSecurityMultifactorWebflowCustomizer(
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(CasMultifactorWebflowCustomizer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new CasMultifactorWebflowCustomizer() {
                    @Override
                    public List<String> getWebflowAttributeMappings() {
                        return List.of(WebUtils.REQUEST_SURROGATE_ACCOUNT_ATTRIBUTE);
                    }
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SurrogateAuthentication)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer")
        public CasWebflowConfigurer surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new SurrogateWebflowConfigurer(
                    flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "surrogateDuoSecurityMultifactorAuthenticationWebflowExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer surrogateDuoSecurityMultifactorAuthenticationWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("surrogateDuoSecurityMultifactorAuthenticationWebflowConfigurer")
            final CasWebflowConfigurer surrogateWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(surrogateWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }

        private static final class SurrogateWebflowConfigurer extends AbstractCasWebflowConfigurer {
            SurrogateWebflowConfigurer(
                final FlowBuilderServices flowBuilderServices,
                final FlowDefinitionRegistry mainFlowDefinitionRegistry,
                final ConfigurableApplicationContext applicationContext,
                final CasConfigurationProperties casProperties) {
                super(flowBuilderServices, mainFlowDefinitionRegistry, applicationContext, casProperties);
                setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            }

            @Override
            protected void doInitialize() {
                val validateAction = getState(getLoginFlow(), CasWebflowConstants.STATE_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN);
                createTransitionForState(validateAction, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    CasWebflowConstants.STATE_ID_LOAD_SURROGATES_ACTION, true);
                val duoConfig = casProperties.getAuthn().getMfa().getDuo();
                duoConfig.forEach(duoCfg -> {
                    val duoSuccess = getState(getLoginFlow(), duoCfg.getId());
                    if (duoSuccess != null) {
                        createTransitionForState(duoSuccess, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                            CasWebflowConstants.STATE_ID_LOAD_SURROGATES_ACTION, true);
                    }
                });
            }
        }
    }


}
