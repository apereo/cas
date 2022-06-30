package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasDefaultFlowUrlHandler;
import org.apereo.cas.web.flow.CasFlowHandlerAdapter;
import org.apereo.cas.web.flow.CasFlowHandlerMapping;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.FlowBuilderConversionService;
import org.apereo.cas.web.flow.configurer.DefaultLoginWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.DefaultLogoutWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.DynamicFlowModelBuilder;
import org.apereo.cas.web.flow.configurer.GroovyWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.plan.DefaultCasWebflowExecutionPlan;
import org.apereo.cas.web.flow.executor.WebflowExecutorFactory;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CasLocaleChangeInterceptor;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.webflow.config.FlowBuilderServicesBuilder;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.ViewFactoryCreator;
import org.springframework.webflow.engine.builder.model.FlowModelFlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.model.builder.DefaultFlowModelHolder;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.expression.spel.WebFlowSpringELExpressionParser;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.servlet.FlowHandlerMapping;

import java.util.ArrayList;
import java.util.List;


/**
 * This is {@link CasWebflowContextConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Webflow)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration
public class CasWebflowContextConfiguration {

    private static final int LOGOUT_FLOW_HANDLER_ORDER = 3;

    private static final FlowExecutionListener[] FLOW_EXECUTION_LISTENERS = new FlowExecutionListener[0];

    @Configuration(value = "CasWebflowContextFlowHandlerConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasWebflowContextFlowHandlerConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FlowUrlHandler loginFlowUrlHandler() {
            return new CasDefaultFlowUrlHandler();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FlowUrlHandler logoutFlowUrlHandler() {
            return new CasDefaultFlowUrlHandler();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public HandlerAdapter logoutHandlerAdapter(
            @Qualifier("logoutFlowUrlHandler")
            final FlowUrlHandler logoutFlowUrlHandler,
            @Qualifier("logoutFlowExecutor")
            final FlowExecutor logoutFlowExecutor) {
            val handler = new CasFlowHandlerAdapter(CasWebflowConfigurer.FLOW_ID_LOGOUT);
            handler.setFlowExecutor(logoutFlowExecutor);
            handler.setFlowUrlHandler(logoutFlowUrlHandler);
            return handler;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerMapping loginFlowHandlerMapping(
            @Qualifier(CasWebflowExecutionPlan.BEAN_NAME)
            final CasWebflowExecutionPlan webflowExecutionPlan,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowRegistry) {
            val handler = new CasFlowHandlerMapping();
            handler.setOrder(LOGOUT_FLOW_HANDLER_ORDER - 1);
            handler.setFlowRegistry(loginFlowRegistry);
            handler.setInterceptors(webflowExecutionPlan.getWebflowInterceptors().toArray());
            return handler;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerAdapter loginHandlerAdapter(
            @Qualifier("loginFlowExecutor")
            final FlowExecutor loginFlowExecutor,
            @Qualifier("loginFlowUrlHandler")
            final FlowUrlHandler loginFlowUrlHandler) {
            val handler = new CasFlowHandlerAdapter(CasWebflowConfigurer.FLOW_ID_LOGIN);
            handler.setFlowExecutor(loginFlowExecutor);
            handler.setFlowUrlHandler(loginFlowUrlHandler);
            return handler;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerMapping logoutFlowHandlerMapping(
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry logoutFlowRegistry,
            @Qualifier("localeChangeInterceptor")
            final LocaleChangeInterceptor localeChangeInterceptor,
            @Qualifier("resourceUrlProviderExposingInterceptor")
            final HandlerInterceptor resourceUrlProviderExposingInterceptor) {
            val handler = new FlowHandlerMapping();
            handler.setOrder(LOGOUT_FLOW_HANDLER_ORDER);
            handler.setFlowRegistry(logoutFlowRegistry);
            handler.setInterceptors(localeChangeInterceptor, resourceUrlProviderExposingInterceptor);
            return handler;
        }

    }

    @Configuration(value = "CasWebflowContextFlowExecutorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasWebflowContextFlowExecutorConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public FlowExecutor logoutFlowExecutor(
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry logoutFlowRegistry,
            @Qualifier("webflowCipherExecutor")
            final CipherExecutor webflowCipherExecutor) {
            val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
                logoutFlowRegistry, webflowCipherExecutor, FLOW_EXECUTION_LISTENERS);
            return factory.build();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public FlowExecutor loginFlowExecutor(
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier("webflowCipherExecutor")
            final CipherExecutor webflowCipherExecutor) {
            val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
                loginFlowRegistry, webflowCipherExecutor,
                FLOW_EXECUTION_LISTENERS);

            return factory.build();
        }
    }

    @Configuration(value = "CasWebflowContextInterceptorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasWebflowContextInterceptorConfiguration {

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "localeChangeInterceptor")
        public LocaleChangeInterceptor localeChangeInterceptor(
            final ObjectProvider<CasConfigurationProperties> casProperties,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ObjectProvider<ServicesManager> servicesManager,
            @Qualifier(ArgumentExtractor.BEAN_NAME)
            final ObjectProvider<ArgumentExtractor> argumentExtractor) {
            val localeProperties = casProperties.getObject().getLocale();
            val interceptor = new CasLocaleChangeInterceptor(casProperties,
                argumentExtractor, servicesManager);
            interceptor.setParamName(localeProperties.getParamName());
            interceptor.setSupportedFlows(List.of(
                CasWebflowConfigurer.FLOW_ID_LOGOUT,
                CasWebflowConfigurer.FLOW_ID_LOGIN));
            return interceptor;
        }

        @Bean
        @ConditionalOnMissingBean(name = "resourceUrlProviderExposingInterceptor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerInterceptor resourceUrlProviderExposingInterceptor(
            @Qualifier("mvcResourceUrlProvider")
            final ResourceUrlProvider resourceUrlProvider) {
            return new ResourceUrlProviderExposingInterceptor(resourceUrlProvider);
        }
    }

    @Configuration(value = "CasWebflowContextExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasWebflowContextExecutionPlanConfiguration {
        @ConditionalOnMissingBean(name = "defaultWebflowConfigurer")
        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer defaultWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry logoutFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            val c = new DefaultLoginWebflowConfigurer(flowBuilderServices, loginFlowRegistry, applicationContext, casProperties);
            c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry);
            c.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return c;
        }

        @ConditionalOnMissingBean(name = "defaultLogoutWebflowConfigurer")
        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer defaultLogoutWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry logoutFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            val c = new DefaultLogoutWebflowConfigurer(flowBuilderServices, loginFlowRegistry, applicationContext, casProperties);
            c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry);
            c.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return c;
        }

        @ConditionalOnMissingBean(name = "groovyWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer groovyWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry logoutFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            val c = new GroovyWebflowConfigurer(flowBuilderServices, loginFlowRegistry, applicationContext, casProperties);
            c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry);
            return c;
        }

        @ConditionalOnMissingBean(name = "casDefaultWebflowExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer casDefaultWebflowExecutionPlanConfigurer(
            @Qualifier("defaultWebflowConfigurer")
            final CasWebflowConfigurer defaultWebflowConfigurer,
            @Qualifier("defaultLogoutWebflowConfigurer")
            final CasWebflowConfigurer defaultLogoutWebflowConfigurer,
            @Qualifier("groovyWebflowConfigurer")
            final CasWebflowConfigurer groovyWebflowConfigurer,
            @Qualifier("localeChangeInterceptor")
            final LocaleChangeInterceptor localeChangeInterceptor,
            @Qualifier("resourceUrlProviderExposingInterceptor")
            final HandlerInterceptor resourceUrlProviderExposingInterceptor,
            @Qualifier("themeChangeInterceptor")
            final ObjectProvider<ThemeChangeInterceptor> themeChangeInterceptor,
            @Qualifier(AuthenticationThrottlingExecutionPlan.BEAN_NAME)
            final ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan) {
            return plan -> {
                plan.registerWebflowConfigurer(defaultWebflowConfigurer);
                plan.registerWebflowConfigurer(defaultLogoutWebflowConfigurer);
                plan.registerWebflowConfigurer(groovyWebflowConfigurer);

                plan.registerWebflowInterceptor(localeChangeInterceptor);
                plan.registerWebflowInterceptor(resourceUrlProviderExposingInterceptor);
                themeChangeInterceptor.ifAvailable(plan::registerWebflowInterceptor);

                authenticationThrottlingExecutionPlan.ifAvailable(
                    throttlingPlan -> throttlingPlan.getAuthenticationThrottleInterceptors().forEach(plan::registerWebflowInterceptor));
            };
        }
    }

    @Configuration(value = "CasWebflowContextBuilderConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasWebflowContextBuilderConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public FlowBuilderServices flowBuilderServices(
            @Qualifier("flowBuilderConversionService")
            final ConversionService flowBuilderConversionService,
            @Qualifier("viewFactoryCreator")
            final ViewFactoryCreator viewFactoryCreator,
            @Qualifier("expressionParser")
            final ExpressionParser expressionParser) {
            val builder = new FlowBuilderServicesBuilder();
            builder.setViewFactoryCreator(viewFactoryCreator);
            builder.setExpressionParser(expressionParser);
            builder.setConversionService(flowBuilderConversionService);
            return builder.build();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExpressionParser expressionParser(
            @Qualifier("flowBuilderConversionService")
            final ConversionService flowBuilderConversionService) {
            return new WebFlowSpringELExpressionParser(new SpelExpressionParser(), flowBuilderConversionService);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ConversionService flowBuilderConversionService(
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
            return new FlowBuilderConversionService(webApplicationServiceFactory);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public ViewFactoryCreator viewFactoryCreator(
            final ObjectProvider<List<ViewResolver>> resolversProvider,
            @Qualifier("registeredServiceViewResolver")
            final ObjectProvider<ViewResolver> registeredServiceViewResolver) {
            val viewResolver = registeredServiceViewResolver.getIfAvailable();
            val resolver = new MvcViewFactoryCreator();
            if (viewResolver != null) {
                resolver.setViewResolvers(CollectionUtils.wrap(viewResolver));
            } else if (resolversProvider.getIfAvailable() != null) {
                val resolvers = new ArrayList<>(resolversProvider.getObject());
                AnnotationAwareOrderComparator.sort(resolvers);
                resolver.setViewResolvers(resolvers);
            }
            return resolver;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FlowBuilder flowBuilder() {
            return new FlowModelFlowBuilder(new DefaultFlowModelHolder(new DynamicFlowModelBuilder()));
        }

    }

    @Configuration(value = "CasWebflowDefinitionsConfiguration", proxyBeanMethods = false)
    public static class CasWebflowDefinitionsConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FlowDefinitionRegistry logoutFlowRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, CasWebflowConfigurer.FLOW_ID_LOGOUT);
            return builder.build();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FlowDefinitionRegistry loginFlowRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, CasWebflowConfigurer.FLOW_ID_LOGIN);
            return builder.build();
        }
    }

    @Configuration(value = "CasWebflowExecutionConfiguration", proxyBeanMethods = false)
    public static class CasWebflowExecutionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlan casWebflowExecutionPlan(final ConfigurableApplicationContext applicationContext) {
            val configurers = applicationContext.getBeansOfType(CasWebflowExecutionPlanConfigurer.class).values();
            val plan = new DefaultCasWebflowExecutionPlan();
            configurers.forEach(c -> c.configureWebflowExecutionPlan(plan));
            plan.execute();
            return plan;
        }
    }
}

