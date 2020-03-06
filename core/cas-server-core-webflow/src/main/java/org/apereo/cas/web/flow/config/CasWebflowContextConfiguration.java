package org.apereo.cas.web.flow.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.throttle.AuthenticationThrottlingExecutionPlan;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.flow.CasDefaultFlowUrlHandler;
import org.apereo.cas.web.flow.CasFlowHandlerAdapter;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.LogoutConversionService;
import org.apereo.cas.web.flow.configurer.DefaultLoginWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.DefaultLogoutWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.GroovyWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.plan.DefaultCasWebflowExecutionPlan;
import org.apereo.cas.web.flow.executor.WebflowExecutorFactory;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
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
import org.springframework.webflow.engine.builder.ViewFactoryCreator;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
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
@Configuration("casWebflowContextConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasWebflowContextConfiguration {

    private static final int LOGOUT_FLOW_HANDLER_ORDER = 3;

    private static final FlowExecutionListener[] FLOW_EXECUTION_LISTENERS = new FlowExecutionListener[0];

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationThrottlingExecutionPlan")
    private ObjectProvider<AuthenticationThrottlingExecutionPlan> authenticationThrottlingExecutionPlan;

    @Autowired
    @Qualifier("registeredServiceViewResolver")
    private ObjectProvider<ViewResolver> registeredServiceViewResolver;
    
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("webflowCipherExecutor")
    private ObjectProvider<CipherExecutor> webflowCipherExecutor;

    @Autowired
    @Qualifier("themeChangeInterceptor")
    private ObjectProvider<ThemeChangeInterceptor> themeChangeInterceptor;

    @Bean
    @Lazy(false)
    public ExpressionParser expressionParser() {
        return new WebFlowSpringELExpressionParser(new SpelExpressionParser(), logoutConversionService());
    }

    @Bean
    @Lazy(false)
    public ConversionService logoutConversionService() {
        return new LogoutConversionService();
    }

    @RefreshScope
    @Bean
    @Lazy(false)
    public ViewFactoryCreator viewFactoryCreator() {
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

    @Lazy(false)
    @Bean
    public FlowUrlHandler loginFlowUrlHandler() {
        return new CasDefaultFlowUrlHandler();
    }

    @Bean
    @Lazy(false)
    public FlowUrlHandler logoutFlowUrlHandler() {
        val handler = new CasDefaultFlowUrlHandler();
        handler.setFlowExecutionKeyParameter("RelayState");
        return handler;
    }

    @RefreshScope
    @Bean
    @Lazy(false)
    public HandlerAdapter logoutHandlerAdapter() {
        val handler = new CasFlowHandlerAdapter(CasWebflowConfigurer.FLOW_ID_LOGOUT);
        handler.setFlowExecutor(logoutFlowExecutor());
        handler.setFlowUrlHandler(logoutFlowUrlHandler());
        return handler;
    }

    @Lazy(false)
    @RefreshScope
    @Bean
    @Qualifier("flowBuilderServices")
    public FlowBuilderServices builder() {
        val builder = new FlowBuilderServicesBuilder();
        builder.setViewFactoryCreator(viewFactoryCreator());
        builder.setExpressionParser(expressionParser());
        builder.setDevelopmentMode(casProperties.getWebflow().isRefresh());
        return builder.build();
    }

    @Lazy(false)
    @Bean
    public HandlerAdapter loginHandlerAdapter() {
        val handler = new CasFlowHandlerAdapter(CasWebflowConfigurer.FLOW_ID_LOGIN);
        handler.setFlowExecutor(loginFlowExecutor());
        handler.setFlowUrlHandler(loginFlowUrlHandler());
        return handler;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "localeChangeInterceptor")
    public LocaleChangeInterceptor localeChangeInterceptor() {
        val bean = new LocaleChangeInterceptor();
        bean.setParamName(this.casProperties.getLocale().getParamName());
        return bean;
    }

    @Lazy(false)
    @Bean
    public HandlerMapping logoutFlowHandlerMapping() {
        val handler = new FlowHandlerMapping();
        handler.setOrder(LOGOUT_FLOW_HANDLER_ORDER);
        handler.setFlowRegistry(logoutFlowRegistry());
        val interceptors = new Object[]{localeChangeInterceptor()};
        handler.setInterceptors(interceptors);
        return handler;
    }

    @Lazy(false)
    @Bean
    public Object[] loginFlowHandlerMappingInterceptors() {
        val interceptors = new ArrayList<Object>();
        interceptors.add(localeChangeInterceptor());
        themeChangeInterceptor.ifAvailable(interceptors::add);
        authenticationThrottlingExecutionPlan.ifAvailable(p -> interceptors.addAll(p.getAuthenticationThrottleInterceptors()));
        return interceptors.toArray();
    }

    @Bean
    @Lazy(false)
    public HandlerMapping loginFlowHandlerMapping() {
        val handler = new FlowHandlerMapping();
        handler.setOrder(LOGOUT_FLOW_HANDLER_ORDER - 1);
        handler.setFlowRegistry(loginFlowRegistry());
        handler.setInterceptors(loginFlowHandlerMappingInterceptors());
        return handler;
    }

    @Lazy(false)
    @Bean
    public FlowDefinitionRegistry logoutFlowRegistry() {
        val basePath = StringUtils.defaultIfBlank(casProperties.getWebflow().getBasePath(), CasWebflowConstants.BASE_CLASSPATH_WEBFLOW);
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, builder());
        builder.setBasePath(basePath);
        builder.addFlowLocationPattern("/logout/*-webflow.xml");
        return builder.build();
    }

    @Lazy(false)
    @Bean
    public FlowDefinitionRegistry loginFlowRegistry() {
        val basePath = StringUtils.defaultIfBlank(casProperties.getWebflow().getBasePath(), CasWebflowConstants.BASE_CLASSPATH_WEBFLOW);
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, builder());
        builder.setBasePath(basePath);
        builder.addFlowLocationPattern("/login/*-webflow.xml");
        return builder.build();
    }

    @RefreshScope
    @Bean
    @Lazy(false)
    public FlowExecutor logoutFlowExecutor() {
        val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
            logoutFlowRegistry(), this.webflowCipherExecutor.getObject(), FLOW_EXECUTION_LISTENERS);
        return factory.build();
    }

    @RefreshScope
    @Bean
    @Lazy(false)
    public FlowExecutor loginFlowExecutor() {
        val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
            loginFlowRegistry(), this.webflowCipherExecutor.getObject(),
            FLOW_EXECUTION_LISTENERS);

        return factory.build();
    }

    @ConditionalOnMissingBean(name = "defaultWebflowConfigurer")
    @Bean
    @Order(0)
    @RefreshScope
    @Lazy(false)
    public CasWebflowConfigurer defaultWebflowConfigurer() {
        val c = new DefaultLoginWebflowConfigurer(builder(), loginFlowRegistry(), applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry());
        c.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return c;
    }

    @ConditionalOnMissingBean(name = "defaultLogoutWebflowConfigurer")
    @Bean
    @Order(0)
    @RefreshScope
    @Lazy(false)
    public CasWebflowConfigurer defaultLogoutWebflowConfigurer() {
        val c = new DefaultLogoutWebflowConfigurer(builder(), loginFlowRegistry(),
            applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry());
        c.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return c;
    }

    @ConditionalOnMissingBean(name = "groovyWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    @RefreshScope
    @Lazy(false)
    public CasWebflowConfigurer groovyWebflowConfigurer() {
        val c = new GroovyWebflowConfigurer(builder(), loginFlowRegistry(), applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry());
        return c;
    }

    @Autowired
    @Bean
    @Lazy(false)
    public CasWebflowExecutionPlan casWebflowExecutionPlan(final List<CasWebflowExecutionPlanConfigurer> configurers) {
        val plan = new DefaultCasWebflowExecutionPlan();
        configurers.forEach(c -> c.configureWebflowExecutionPlan(plan));
        plan.execute();
        return plan;
    }

    @ConditionalOnMissingBean(name = "casDefaultWebflowExecutionPlanConfigurer")
    @Bean
    @Lazy(false)
    public CasWebflowExecutionPlanConfigurer casDefaultWebflowExecutionPlanConfigurer() {
        return plan -> {
            plan.registerWebflowConfigurer(defaultWebflowConfigurer());
            plan.registerWebflowConfigurer(defaultLogoutWebflowConfigurer());
            plan.registerWebflowConfigurer(groovyWebflowConfigurer());
        };
    }
}

