package org.apereo.cas.web.flow.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.flow.CasDefaultFlowUrlHandler;
import org.apereo.cas.web.flow.CasFlowHandlerAdapter;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.LogoutConversionService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
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
@Configuration(value = "casWebflowContextConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class CasWebflowContextConfiguration {

    private static final int LOGOUT_FLOW_HANDLER_ORDER = 3;

    private static final FlowExecutionListener[] FLOW_EXECUTION_LISTENERS = new FlowExecutionListener[0];

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationThrottlingExecutionPlan")
    private ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Bean
    public FlowUrlHandler loginFlowUrlHandler() {
        return new CasDefaultFlowUrlHandler();
    }

    @Bean
    public FlowUrlHandler logoutFlowUrlHandler() {
        return new CasDefaultFlowUrlHandler();
    }

    @RefreshScope
    @Bean
    @Autowired
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
    @Autowired
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

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "localeChangeInterceptor")
    public LocaleChangeInterceptor localeChangeInterceptor() {
        val interceptor = new CasLocaleChangeInterceptor(casProperties.getLocale(),
            argumentExtractor.getObject(), servicesManager.getObject());
        interceptor.setParamName(casProperties.getLocale().getParamName());
        interceptor.setSupportedFlows(List.of(
            CasWebflowConfigurer.FLOW_ID_LOGOUT,
            CasWebflowConfigurer.FLOW_ID_LOGIN));
        return interceptor;
    }

    @Bean
    @Autowired
    public HandlerMapping logoutFlowHandlerMapping(
        @Qualifier("logoutFlowRegistry")
        final FlowDefinitionRegistry logoutFlowRegistry,
        @Qualifier("localeChangeInterceptor")
        final LocaleChangeInterceptor localeChangeInterceptor) {
        val handler = new FlowHandlerMapping();
        handler.setOrder(LOGOUT_FLOW_HANDLER_ORDER);
        handler.setFlowRegistry(logoutFlowRegistry);
        handler.setInterceptors(localeChangeInterceptor);
        return handler;
    }


    @RefreshScope
    @Bean
    @Autowired
    public FlowExecutor logoutFlowExecutor(
        @Qualifier("logoutFlowRegistry")
        final FlowDefinitionRegistry logoutFlowRegistry,
        @Qualifier("webflowCipherExecutor")
        final CipherExecutor webflowCipherExecutor) {
        val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
            logoutFlowRegistry, webflowCipherExecutor, FLOW_EXECUTION_LISTENERS);
        return factory.build();
    }

    @RefreshScope
    @Bean
    @Autowired
    public FlowExecutor loginFlowExecutor(
        @Qualifier("loginFlowRegistry")
        final FlowDefinitionRegistry loginFlowRegistry,
        @Qualifier("webflowCipherExecutor")
        final CipherExecutor webflowCipherExecutor) {
        val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
            loginFlowRegistry, webflowCipherExecutor,
            FLOW_EXECUTION_LISTENERS);

        return factory.build();
    }

    @ConditionalOnMissingBean(name = "defaultWebflowConfigurer")
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Autowired
    public CasWebflowConfigurer defaultWebflowConfigurer(
        @Qualifier("loginFlowRegistry")
        final FlowDefinitionRegistry loginFlowRegistry,
        @Qualifier("logoutFlowRegistry")
        final FlowDefinitionRegistry logoutFlowRegistry,
        @Qualifier("flowBuilderServices")
        final FlowBuilderServices flowBuilderServices) {
        val c = new DefaultLoginWebflowConfigurer(flowBuilderServices, loginFlowRegistry, applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry);
        c.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return c;
    }

    @ConditionalOnMissingBean(name = "defaultLogoutWebflowConfigurer")
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Autowired
    public CasWebflowConfigurer defaultLogoutWebflowConfigurer(
        @Qualifier("loginFlowRegistry")
        final FlowDefinitionRegistry loginFlowRegistry,
        @Qualifier("logoutFlowRegistry")
        final FlowDefinitionRegistry logoutFlowRegistry,
        @Qualifier("flowBuilderServices")
        final FlowBuilderServices flowBuilderServices) {
        val c = new DefaultLogoutWebflowConfigurer(flowBuilderServices, loginFlowRegistry,
            applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry);
        c.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return c;
    }

    @ConditionalOnMissingBean(name = "groovyWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    @RefreshScope
    @Autowired
    public CasWebflowConfigurer groovyWebflowConfigurer(
        @Qualifier("loginFlowRegistry")
        final FlowDefinitionRegistry loginFlowRegistry,
        @Qualifier("logoutFlowRegistry")
        final FlowDefinitionRegistry logoutFlowRegistry,
        @Qualifier("flowBuilderServices")
        final FlowBuilderServices flowBuilderServices) {
        val c = new GroovyWebflowConfigurer(flowBuilderServices, loginFlowRegistry, applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry);
        return c;
    }

    @ConditionalOnMissingBean(name = "casDefaultWebflowExecutionPlanConfigurer")
    @Bean
    @Autowired
    public CasWebflowExecutionPlanConfigurer casDefaultWebflowExecutionPlanConfigurer(
        @Qualifier("defaultWebflowConfigurer")
        final CasWebflowConfigurer defaultWebflowConfigurer,
        @Qualifier("defaultLogoutWebflowConfigurer")
        final CasWebflowConfigurer defaultLogoutWebflowConfigurer,
        @Qualifier("groovyWebflowConfigurer")
        final CasWebflowConfigurer groovyWebflowConfigurer,
        @Qualifier("localeChangeInterceptor")
        final LocaleChangeInterceptor localeChangeInterceptor,
        @Qualifier("themeChangeInterceptor")
        final ObjectProvider<ThemeChangeInterceptor> themeChangeInterceptor) {
        return plan -> {
            plan.registerWebflowConfigurer(defaultWebflowConfigurer);
            plan.registerWebflowConfigurer(defaultLogoutWebflowConfigurer);
            plan.registerWebflowConfigurer(groovyWebflowConfigurer);

            plan.registerWebflowInterceptor(localeChangeInterceptor);
            themeChangeInterceptor.ifAvailable(plan::registerWebflowInterceptor);

            authenticationThrottlingExecutionPlan.ifAvailable(
                throttlingPlan -> throttlingPlan.getAuthenticationThrottleInterceptors().forEach(plan::registerWebflowInterceptor));
        };
    }

    @Configuration(value = "CasWebflowDefinitionsConfiguration", proxyBeanMethods = false)
    @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    public static class CasWebflowDefinitionsConfiguration {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Bean
        public FlowBuilder flowBuilder() {
            return new FlowModelFlowBuilder(new DefaultFlowModelHolder(new DynamicFlowModelBuilder()));
        }

        @Bean
        @Autowired
        public FlowDefinitionRegistry logoutFlowRegistry(
            @Qualifier("flowBuilderServices")
            final FlowBuilderServices flowBuilderServices,
            @Qualifier("flowBuilder")
            final FlowBuilder flowBuilder) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, CasWebflowConfigurer.FLOW_ID_LOGOUT);
            return builder.build();
        }

        @Bean
        @Autowired
        public FlowDefinitionRegistry loginFlowRegistry(
            @Qualifier("flowBuilderServices")
            final FlowBuilderServices flowBuilderServices,
            @Qualifier("flowBuilder")
            final FlowBuilder flowBuilder) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, CasWebflowConfigurer.FLOW_ID_LOGIN);
            return builder.build();
        }

        @RefreshScope
        @Bean
        @Autowired
        public FlowBuilderServices flowBuilderServices(
            @Qualifier("viewFactoryCreator")
            final ViewFactoryCreator viewFactoryCreator,
            @Qualifier("expressionParser")
            final ExpressionParser expressionParser) {
            val builder = new FlowBuilderServicesBuilder();
            builder.setViewFactoryCreator(viewFactoryCreator);
            builder.setExpressionParser(expressionParser);
            return builder.build();
        }

        @Bean
        @Autowired
        public ExpressionParser expressionParser(
            @Qualifier("logoutConversionService")
            final ConversionService logoutConversionService) {
            return new WebFlowSpringELExpressionParser(new SpelExpressionParser(), logoutConversionService);
        }

        @Bean
        public ConversionService logoutConversionService() {
            return new LogoutConversionService();
        }

        @RefreshScope
        @Bean
        @Autowired
        public ViewFactoryCreator viewFactoryCreator(
            @Qualifier("registeredServiceViewResolver")
            final ObjectProvider<ViewResolver> registeredServiceViewResolver) {
            val viewResolver = registeredServiceViewResolver.getIfAvailable();
            val resolver = new MvcViewFactoryCreator();
            if (viewResolver != null) {
                resolver.setViewResolvers(CollectionUtils.wrap(viewResolver));
            } else {
                val resolverBeans = applicationContext.getBeansOfType(ViewResolver.class, false, true);
                val resolvers = new ArrayList<>(resolverBeans.values());
                AnnotationAwareOrderComparator.sort(resolvers);
                resolver.setViewResolvers(resolvers);
            }
            return resolver;
        }
    }

    @Configuration(value = "CasWebflowExecutionConfiguration", proxyBeanMethods = false)
    public static class CasWebflowExecutionConfiguration {
        @Bean
        @Autowired
        public HandlerMapping loginFlowHandlerMapping(
            @Qualifier(CasWebflowExecutionPlan.BEAN_NAME)
            final CasWebflowExecutionPlan webflowExecutionPlan,
            @Qualifier("loginFlowRegistry")
            final FlowDefinitionRegistry loginFlowRegistry) {
            val handler = new FlowHandlerMapping();
            handler.setOrder(LOGOUT_FLOW_HANDLER_ORDER - 1);
            handler.setFlowRegistry(loginFlowRegistry);
            handler.setInterceptors(webflowExecutionPlan.getWebflowInterceptors().toArray());
            return handler;
        }

        @Autowired
        @Bean
        public CasWebflowExecutionPlan casWebflowExecutionPlan(final List<CasWebflowExecutionPlanConfigurer> configurers) {
            val plan = new DefaultCasWebflowExecutionPlan();
            configurers.forEach(c -> c.configureWebflowExecutionPlan(plan));
            plan.execute();
            return plan;
        }
    }
}

