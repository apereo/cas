package org.apereo.cas.web.flow.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.actions.CasDefaultFlowUrlHandler;
import org.apereo.cas.web.flow.actions.LogoutConversionService;
import org.apereo.cas.web.flow.configurer.DefaultLoginWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.DefaultLogoutWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.GroovyWebflowConfigurer;
import org.apereo.cas.web.flow.executor.WebflowExecutorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.webflow.config.FlowBuilderServicesBuilder;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.ViewFactoryCreator;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.expression.spel.WebFlowSpringELExpressionParser;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.servlet.FlowHandler;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;
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
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CasWebflowContextConfiguration {

    private static final int LOGOUT_FLOW_HANDLER_ORDER = 3;

    private static final String BASE_CLASSPATH_WEBFLOW = "classpath*:/webflow";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("registeredServiceViewResolver")
    private ViewResolver registeredServiceViewResolver;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("webflowCipherExecutor")
    private CipherExecutor webflowCipherExecutor;

    @Bean
    public ExpressionParser expressionParser() {
        return new WebFlowSpringELExpressionParser(new SpelExpressionParser(), logoutConversionService());
    }

    @Bean
    public ConversionService logoutConversionService() {
        return new LogoutConversionService();
    }

    @RefreshScope
    @Bean
    public ViewFactoryCreator viewFactoryCreator() {
        final MvcViewFactoryCreator resolver = new MvcViewFactoryCreator();
        resolver.setViewResolvers(CollectionUtils.wrap(this.registeredServiceViewResolver));
        return resolver;
    }

    @Bean
    public FlowUrlHandler loginFlowUrlHandler() {
        return new CasDefaultFlowUrlHandler();
    }

    @Bean
    public FlowUrlHandler logoutFlowUrlHandler() {
        final CasDefaultFlowUrlHandler handler = new CasDefaultFlowUrlHandler();
        handler.setFlowExecutionKeyParameter("RelayState");
        return handler;
    }

    @RefreshScope
    @Bean
    public HandlerAdapter logoutHandlerAdapter() {
        final FlowHandlerAdapter handler = new FlowHandlerAdapter() {
            @Override
            public boolean supports(final Object handler) {
                return super.supports(handler) && ((FlowHandler) handler).getFlowId().equals(CasWebflowConfigurer.FLOW_ID_LOGOUT);
            }
        };
        handler.setFlowExecutor(logoutFlowExecutor());
        handler.setFlowUrlHandler(logoutFlowUrlHandler());
        return handler;
    }

    @RefreshScope
    @Bean
    public FlowBuilderServices builder() {
        final FlowBuilderServicesBuilder builder = new FlowBuilderServicesBuilder(this.applicationContext);
        builder.setViewFactoryCreator(viewFactoryCreator());
        builder.setExpressionParser(expressionParser());
        builder.setDevelopmentMode(casProperties.getWebflow().isRefresh());
        return builder.build();
    }


    @Bean
    public HandlerAdapter loginHandlerAdapter() {
        final FlowHandlerAdapter handler = new FlowHandlerAdapter() {
            @Override
            public boolean supports(final Object handler) {
                return super.supports(handler) && ((FlowHandler) handler)
                    .getFlowId().equals(CasWebflowConfigurer.FLOW_ID_LOGIN);
            }
        };
        handler.setFlowExecutor(loginFlowExecutor());
        handler.setFlowUrlHandler(loginFlowUrlHandler());
        return handler;
    }

    @RefreshScope
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        final LocaleChangeInterceptor bean = new LocaleChangeInterceptor();
        bean.setParamName(this.casProperties.getLocale().getParamName());
        return bean;
    }

    @Bean
    public HandlerMapping logoutFlowHandlerMapping() {
        final FlowHandlerMapping handler = new FlowHandlerMapping();
        handler.setOrder(LOGOUT_FLOW_HANDLER_ORDER);
        handler.setFlowRegistry(logoutFlowRegistry());
        final Object[] interceptors = new Object[]{localeChangeInterceptor()};
        handler.setInterceptors(interceptors);
        return handler;
    }

    @Lazy
    @Bean
    public Object[] loginFlowHandlerMappingInterceptors() {
        final List interceptors = new ArrayList<>();
        interceptors.add(localeChangeInterceptor());
        if (this.applicationContext.containsBean("authenticationThrottle")) {
            interceptors.add(this.applicationContext.getBean("authenticationThrottle", HandlerInterceptor.class));
        }
        return interceptors.toArray();
    }

    @Bean
    public HandlerMapping loginFlowHandlerMapping() {
        final FlowHandlerMapping handler = new FlowHandlerMapping();
        handler.setOrder(LOGOUT_FLOW_HANDLER_ORDER - 1);
        handler.setFlowRegistry(loginFlowRegistry());
        handler.setInterceptors(loginFlowHandlerMappingInterceptors());
        return handler;
    }

    @Bean
    public FlowDefinitionRegistry logoutFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, builder());
        builder.setBasePath(BASE_CLASSPATH_WEBFLOW);
        builder.addFlowLocationPattern("/logout/*-webflow.xml");
        return builder.build();
    }

    @Bean
    public FlowDefinitionRegistry loginFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, builder());
        builder.setBasePath(BASE_CLASSPATH_WEBFLOW);
        builder.addFlowLocationPattern("/login/*-webflow.xml");
        return builder.build();
    }

    @RefreshScope
    @Bean
    public FlowExecutor logoutFlowExecutor() {
        final WebflowExecutorFactory factory = new WebflowExecutorFactory(casProperties.getWebflow(),
            logoutFlowRegistry(), this.webflowCipherExecutor);
        return factory.build();
    }

    @RefreshScope
    @Bean
    public FlowExecutor loginFlowExecutor() {
        final WebflowExecutorFactory factory = new WebflowExecutorFactory(casProperties.getWebflow(),
            loginFlowRegistry(), this.webflowCipherExecutor);
        return factory.build();
    }

    @ConditionalOnMissingBean(name = "defaultWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer defaultWebflowConfigurer() {
        final DefaultLoginWebflowConfigurer c = new DefaultLoginWebflowConfigurer(builder(), loginFlowRegistry(), applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry());
        c.initialize();
        return c;
    }

    @ConditionalOnMissingBean(name = "defaultLogoutWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer defaultLogoutWebflowConfigurer() {
        final DefaultLogoutWebflowConfigurer c = new DefaultLogoutWebflowConfigurer(builder(), loginFlowRegistry(),
            applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry());
        c.initialize();
        return c;
    }

    @ConditionalOnMissingBean(name = "groovyWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer groovyWebflowConfigurer() {
        final GroovyWebflowConfigurer c = new GroovyWebflowConfigurer(builder(), loginFlowRegistry(), applicationContext, casProperties);
        c.setLogoutFlowDefinitionRegistry(logoutFlowRegistry());
        c.initialize();
        return c;
    }
}

