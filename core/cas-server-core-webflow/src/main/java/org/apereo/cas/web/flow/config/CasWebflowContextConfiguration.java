package org.apereo.cas.web.flow.config;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasFlowHandlerAdapter;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.actions.CasDefaultFlowUrlHandler;
import org.apereo.cas.web.flow.actions.LogoutConversionService;
import org.apereo.cas.web.flow.configurer.DefaultWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.GroovyWebflowConfigurer;
import org.apereo.spring.webflow.plugin.ClientFlowExecutionRepository;
import org.apereo.spring.webflow.plugin.EncryptedTranscoder;
import org.apereo.spring.webflow.plugin.Transcoder;
import org.cryptacular.bean.CipherBean;
import org.springframework.beans.factory.BeanCreationException;
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
import org.springframework.webflow.config.FlowExecutorBuilder;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.conversation.impl.SessionBindingConversationManager;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.ViewFactoryCreator;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.execution.repository.impl.DefaultFlowExecutionRepository;
import org.springframework.webflow.execution.repository.snapshot.SerializedFlowExecutionSnapshotFactory;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.expression.spel.WebFlowSpringELExpressionParser;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.servlet.FlowHandlerAdapter;
import org.springframework.webflow.mvc.servlet.FlowHandlerMapping;

import javax.naming.OperationNotSupportedException;
import java.io.InputStream;
import java.io.OutputStream;
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
        return new WebFlowSpringELExpressionParser(
            new SpelExpressionParser(),
            logoutConversionService());
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
        final FlowHandlerAdapter handler = new CasFlowHandlerAdapter(CasWebflowConfigurer.FLOW_ID_LOGOUT);
        handler.setFlowExecutor(logoutFlowExecutor());
        handler.setFlowUrlHandler(logoutFlowUrlHandler());
        return handler;
    }

    @RefreshScope
    @Bean
    public CipherBean loginFlowCipherBean() {
        try {
            return new CipherBean() {
                @Override
                public byte[] encrypt(final byte[] bytes) {
                    return (byte[]) CasWebflowContextConfiguration.this.webflowCipherExecutor.encode(bytes);
                }

                @Override
                public void encrypt(final InputStream inputStream, final OutputStream outputStream) {
                    throw new IllegalArgumentException(
                        new OperationNotSupportedException("Encrypting input stream is not supported"));
                }

                @Override
                public byte[] decrypt(final byte[] bytes) {
                    return (byte[]) CasWebflowContextConfiguration.this.webflowCipherExecutor.decode(bytes);
                }

                @Override
                public void decrypt(final InputStream inputStream, final OutputStream outputStream) {
                    throw new IllegalArgumentException(
                        new OperationNotSupportedException("Decrypting input stream is not supported"));
                }
            };
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
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
    public Transcoder loginFlowStateTranscoder() {
        try {
            return new EncryptedTranscoder(loginFlowCipherBean());
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    public HandlerAdapter loginHandlerAdapter() {
        final FlowHandlerAdapter handler = new CasFlowHandlerAdapter(CasWebflowConfigurer.FLOW_ID_LOGIN);
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

    @RefreshScope
    @Bean
    public FlowExecutor logoutFlowExecutor() {
        final FlowExecutorBuilder builder = new FlowExecutorBuilder(logoutFlowRegistry(), this.applicationContext);
        builder.setAlwaysRedirectOnPause(casProperties.getWebflow().isAlwaysPauseRedirect());
        builder.setRedirectInSameState(casProperties.getWebflow().isRedirectSameState());
        return builder.build();
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
    public FlowExecutor loginFlowExecutor() {
        if (casProperties.getWebflow().getSession().isStorage()) {
            return flowExecutorViaServerSessionBindingExecution();
        }
        return flowExecutorViaClientFlowExecution();
    }

    @Bean
    public FlowExecutor flowExecutorViaServerSessionBindingExecution() {
        final FlowDefinitionRegistry loginFlowRegistry = loginFlowRegistry();

        final SessionBindingConversationManager conversationManager = new SessionBindingConversationManager();
        conversationManager.setLockTimeoutSeconds((int) casProperties.getWebflow().getSession().getLockTimeout());
        conversationManager.setMaxConversations(casProperties.getWebflow().getSession().getMaxConversations());

        final FlowExecutionImplFactory executionFactory = new FlowExecutionImplFactory();
        final SerializedFlowExecutionSnapshotFactory flowExecutionSnapshotFactory =
            new SerializedFlowExecutionSnapshotFactory(executionFactory, loginFlowRegistry);
        flowExecutionSnapshotFactory.setCompress(casProperties.getWebflow().getSession().isCompress());

        final DefaultFlowExecutionRepository repository = new DefaultFlowExecutionRepository(conversationManager,
            flowExecutionSnapshotFactory);
        executionFactory.setExecutionKeyFactory(repository);
        return new FlowExecutorImpl(loginFlowRegistry, executionFactory, repository);
    }

    @Bean
    public FlowExecutor flowExecutorViaClientFlowExecution() {
        final FlowDefinitionRegistry loginFlowRegistry = loginFlowRegistry();
        final ClientFlowExecutionRepository repository = new ClientFlowExecutionRepository();
        repository.setFlowDefinitionLocator(loginFlowRegistry);
        repository.setTranscoder(loginFlowStateTranscoder());

        final FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
        factory.setExecutionKeyFactory(repository);
        repository.setFlowExecutionFactory(factory);
        return new FlowExecutorImpl(loginFlowRegistry, factory, repository);
    }

    @ConditionalOnMissingBean(name = "defaultWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer defaultWebflowConfigurer() {
        final DefaultWebflowConfigurer c = new DefaultWebflowConfigurer(builder(), loginFlowRegistry(), applicationContext, casProperties);
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

