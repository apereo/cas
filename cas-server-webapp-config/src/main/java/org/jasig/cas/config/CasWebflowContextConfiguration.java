package org.jasig.cas.config;

import com.google.common.collect.ImmutableList;
import org.cryptacular.bean.CipherBean;
import org.jasig.cas.CipherExecutor;
import org.jasig.cas.web.flow.CasDefaultFlowUrlHandler;
import org.jasig.cas.web.flow.LogoutConversionService;
import org.jasig.cas.web.flow.SelectiveFlowHandlerAdapter;
import org.jasig.spring.webflow.plugin.ClientFlowExecutionRepository;
import org.jasig.spring.webflow.plugin.EncryptedTranscoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.binding.convert.ConversionService;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.webflow.config.FlowBuilderServicesBuilder;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.config.FlowExecutorBuilder;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.conversation.impl.SessionBindingConversationManager;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.impl.FlowExecutionImplFactory;
import org.springframework.webflow.execution.repository.impl.DefaultFlowExecutionRepository;
import org.springframework.webflow.execution.repository.snapshot.SerializedFlowExecutionSnapshotFactory;
import org.springframework.webflow.executor.FlowExecutor;
import org.springframework.webflow.executor.FlowExecutorImpl;
import org.springframework.webflow.expression.spel.WebFlowSpringELExpressionParser;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.servlet.FlowHandlerMapping;

import javax.naming.OperationNotSupportedException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is {@link CasWebflowContextConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Configuration("casWebflowContextConfiguration")
public class CasWebflowContextConfiguration {

    private static final int LOGOUT_FLOW_HANDLER_ORDER = 3;
    private static final String BASE_CLASSPATH_WEBFLOW = "classpath*:/webflow";

    @Autowired
    @Qualifier("registeredServiceViewResolver")
    private ViewResolver registeredServiceViewResolver;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${webflow.always.pause.redirect:false}")
    private boolean alwaysPauseOnRedirect;

    @Value("${webflow.redirect.same.state:false}")
    private boolean redirectSameState;


    @Value("${webflow.session.storage:false}")
    private boolean webflowSessionStorage;
    
    @Autowired
    @Qualifier("webflowCipherExecutor")
    private CipherExecutor<byte[], byte[]> webflowCipherExecutor;

    @Autowired
    @Qualifier("authenticationThrottle")
    private HandlerInterceptor authenticationThrottle;

    @Value("${webflow.session.lock.timeout:30}")
    private int webflowSessionLockTimeout;

    @Value("${webflow.session.max.conversations:5}")
    private int webflowSessionMaxConversations;

    @Value("${webflow.session.compress:false}")
    private boolean webflowSessionCompress;

    /**
     * Expression parser web flow spring el expression parser.
     *
     * @return the web flow spring el expression parser
     */
    @Bean(name = "expressionParser")
    public WebFlowSpringELExpressionParser expressionParser() {
        final WebFlowSpringELExpressionParser parser = new WebFlowSpringELExpressionParser(
                new SpelExpressionParser(),
                logoutConversionService());
        return parser;
    }

    /**
     * Logout conversion service conversion service.
     *
     * @return the conversion service
     */
    @Bean(name = "logoutConversionService")
    public ConversionService logoutConversionService() {
        return new LogoutConversionService();
    }

    /**
     * View factory creator mvc view factory creator.
     *
     * @return the mvc view factory creator
     */
    @RefreshScope
    @Bean(name = "viewFactoryCreator")
    public MvcViewFactoryCreator viewFactoryCreator() {
        final MvcViewFactoryCreator resolver = new MvcViewFactoryCreator();
        resolver.setViewResolvers(ImmutableList.of(this.registeredServiceViewResolver));
        return resolver;
    }

    /**
     * Login flow url handler cas default flow url handler.
     *
     * @return the cas default flow url handler
     */
    @Bean(name = "loginFlowUrlHandler")
    public CasDefaultFlowUrlHandler loginFlowUrlHandler() {
        return new CasDefaultFlowUrlHandler();
    }

    /**
     * Logout flow url handler cas default flow url handler.
     *
     * @return the cas default flow url handler
     */
    @Bean(name = "logoutFlowUrlHandler")
    public FlowUrlHandler logoutFlowUrlHandler() {
        final CasDefaultFlowUrlHandler handler = new CasDefaultFlowUrlHandler();
        handler.setFlowExecutionKeyParameter("RelayState");
        return handler;
    }

    /**
     * Logout handler adapter selective flow handler adapter.
     *
     * @return the selective flow handler adapter
     */
    @RefreshScope
    @Bean(name = "logoutHandlerAdapter")
    public SelectiveFlowHandlerAdapter logoutHandlerAdapter() {
        final SelectiveFlowHandlerAdapter handler = new SelectiveFlowHandlerAdapter();
        handler.setSupportedFlowId("logout");
        handler.setFlowExecutor(logoutFlowExecutor());
        handler.setFlowUrlHandler(logoutFlowUrlHandler());
        return handler;
    }

    /**
     * Login flow cipher bean buffered block cipher bean.
     *
     * @return the buffered block cipher bean
     */
    @RefreshScope
    @Bean(name = "loginFlowCipherBean")
    public CipherBean loginFlowCipherBean() {

        try {
            return new CipherBean() {
                @Override
                public byte[] encrypt(final byte[] bytes) {
                    return CasWebflowContextConfiguration.this.webflowCipherExecutor.encode(bytes);
                }

                @Override
                public void encrypt(final InputStream inputStream, final OutputStream outputStream) {
                    throw new RuntimeException(new OperationNotSupportedException("Encrypting input stream is not supported"));
                }

                @Override
                public byte[] decrypt(final byte[] bytes) {
                    return CasWebflowContextConfiguration.this.webflowCipherExecutor.decode(bytes);
                }

                @Override
                public void decrypt(final InputStream inputStream, final OutputStream outputStream) {
                    throw new RuntimeException(new OperationNotSupportedException("Decrypting input stream is not supported"));
                }
            };
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builder flow builder services.
     *
     * @return the flow builder services
     */
    @Bean(name = "builder")
    public FlowBuilderServices builder() {
        final FlowBuilderServicesBuilder builder = new FlowBuilderServicesBuilder(this.applicationContext);
        builder.setViewFactoryCreator(viewFactoryCreator());
        builder.setExpressionParser(expressionParser());
        builder.setDevelopmentMode(true);
        return builder.build();
    }

    /**
     * Login flow state transcoder encrypted transcoder.
     *
     * @return the encrypted transcoder
     */
    @Bean(name = "loginFlowStateTranscoder")
    public EncryptedTranscoder loginFlowStateTranscoder() {
        try {
            return new EncryptedTranscoder(loginFlowCipherBean());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Login handler adapter selective flow handler adapter.
     *
     * @return the selective flow handler adapter
     */
    @Bean(name = "loginHandlerAdapter")
    public SelectiveFlowHandlerAdapter loginHandlerAdapter() {
        final SelectiveFlowHandlerAdapter handler = new SelectiveFlowHandlerAdapter();
        handler.setSupportedFlowId("login");
        handler.setFlowExecutor(loginFlowExecutor());
        handler.setFlowUrlHandler(loginFlowUrlHandler());
        return handler;
    }

    /**
     * Locale change interceptor locale change interceptor.
     *
     * @return the locale change interceptor
     */
    @Bean(name = "localeChangeInterceptor")
    public LocaleChangeInterceptor localeChangeInterceptor() {
        return new LocaleChangeInterceptor();
    }

    /**
     * Logout flow handler mapping flow handler mapping.
     *
     * @return the flow handler mapping
     */
    @Bean(name = "logoutFlowHandlerMapping")
    public FlowHandlerMapping logoutFlowHandlerMapping() {
        final FlowHandlerMapping handler = new FlowHandlerMapping();
        handler.setOrder(LOGOUT_FLOW_HANDLER_ORDER);
        handler.setFlowRegistry(logoutFlowRegistry());
        final Object[] interceptors = new Object[]{localeChangeInterceptor()};
        handler.setInterceptors(interceptors);
        return handler;
    }

    /**
     * Login flow handler mapping flow handler mapping.
     *
     * @return the flow handler mapping
     */
    @Bean(name = "loginFlowHandlerMapping")
    public FlowHandlerMapping loginFlowHandlerMapping() {
        final FlowHandlerMapping handler = new FlowHandlerMapping();
        handler.setOrder(LOGOUT_FLOW_HANDLER_ORDER - 1);
        handler.setFlowRegistry(loginFlowRegistry());
        final Object[] interceptors = new Object[]{localeChangeInterceptor(), this.authenticationThrottle};
        handler.setInterceptors(interceptors);
        return handler;
    }

    /**
     * Logout flow executor flow executor.
     *
     * @return the flow executor
     */
    @RefreshScope
    @Bean(name = "logoutFlowExecutor")
    public FlowExecutor logoutFlowExecutor() {
        final FlowExecutorBuilder builder = new FlowExecutorBuilder(logoutFlowRegistry(), this.applicationContext);
        builder.setAlwaysRedirectOnPause(this.alwaysPauseOnRedirect);
        builder.setRedirectInSameState(this.redirectSameState);
        return builder.build();
    }

    /**
     * Logout flow registry flow definition registry.
     *
     * @return the flow definition registry
     */
    @RefreshScope
    @Bean(name = "logoutFlowRegistry")
    public FlowDefinitionRegistry logoutFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, builder());
        builder.setBasePath(BASE_CLASSPATH_WEBFLOW);
        builder.addFlowLocationPattern("/logout/*-webflow.xml");
        return builder.build();
    }

    /**
     * Login flow registry flow definition registry.
     *
     * @return the flow definition registry
     */
    @RefreshScope
    @Bean(name = "loginFlowRegistry")
    public FlowDefinitionRegistry loginFlowRegistry() {
        final FlowDefinitionRegistryBuilder builder = new FlowDefinitionRegistryBuilder(this.applicationContext, builder());
        builder.setBasePath(BASE_CLASSPATH_WEBFLOW);
        builder.addFlowLocationPattern("/login/*-webflow.xml");
        return builder.build();
    }

    /**
     * Login flow executor flow executor.
     *
     * @return the flow executor
     */
    @RefreshScope
    @Bean(name = "loginFlowExecutor")
    
    public FlowExecutorImpl loginFlowExecutor() {
        if (this.webflowSessionStorage) {
            final SessionBindingConversationManager conversationManager = new SessionBindingConversationManager();
            conversationManager.setLockTimeoutSeconds(this.webflowSessionLockTimeout);
            conversationManager.setMaxConversations(this.webflowSessionMaxConversations);
            
            final FlowExecutionImplFactory executionFactory = new FlowExecutionImplFactory();
            
            final SerializedFlowExecutionSnapshotFactory flowExecutionSnapshotFactory =
                    new SerializedFlowExecutionSnapshotFactory(executionFactory, loginFlowRegistry());
            flowExecutionSnapshotFactory.setCompress(this.webflowSessionCompress);
            
            final DefaultFlowExecutionRepository repository = new DefaultFlowExecutionRepository(conversationManager,
                    flowExecutionSnapshotFactory);
            executionFactory.setExecutionKeyFactory(repository);
            return new FlowExecutorImpl(loginFlowRegistry(), executionFactory, repository);
        }

        final ClientFlowExecutionRepository repository = new ClientFlowExecutionRepository();
        repository.setFlowDefinitionLocator(loginFlowRegistry());
        repository.setTranscoder(loginFlowStateTranscoder());

        final FlowExecutionImplFactory factory = new FlowExecutionImplFactory();
        factory.setExecutionKeyFactory(repository);
        repository.setFlowExecutionFactory(factory);
        return new FlowExecutorImpl(loginFlowRegistry(), factory, repository);
    }
}

