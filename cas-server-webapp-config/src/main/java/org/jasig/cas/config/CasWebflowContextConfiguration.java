package org.jasig.cas.config;

import com.google.common.collect.ImmutableList;
import org.cryptacular.bean.CipherBean;
import org.jasig.cas.CipherExecutor;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.web.RegisteredServiceThemeBasedViewResolver;
import org.jasig.cas.web.flow.CasDefaultFlowUrlHandler;
import org.jasig.cas.web.flow.LogoutConversionService;
import org.jasig.cas.web.flow.SelectiveFlowHandlerAdapter;
import org.jasig.spring.webflow.plugin.EncryptedTranscoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.binding.convert.ConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.ResourceBundleViewResolver;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.XmlViewResolver;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.executor.FlowExecutor;
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
@Configuration("casWebflowContextConfiguration")
@Lazy(true)
public class CasWebflowContextConfiguration {

    private static final int LOGOUT_FLOW_HANDLER_ORDER = 3;
    private static final int VIEW_RESOLVER_RESOURCE_ORDER = 100;
    private static final int VIEW_RESOLVER_BEAN_ORDER = 101;
    private static final int VIEW_RESOLVER_XML_ORDER = 102;
    private static final int VIEW_RESOLVER_SERVICE_ORDER = 103;
    private static final int VIEW_RESOLVER_URL_ORDER = 104;
    private static final int VIEW_RESOLVER_INTERNAL_ORDER = 105;
    
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
    @Value("${cas.themeResolver.pathprefix:/WEB-INF/view/jsp}/default/ui/")
    private String resolverPathPrefix;
    
    @Autowired
    @Qualifier("logoutFlowExecutor")
    @Lazy(true)
    private FlowExecutor logoutFlowExecutor;

    @Autowired
    @Qualifier("loginFlowExecutor")
    @Lazy(true)
    private FlowExecutor loginFlowExecutor;

    @Autowired
    @Qualifier("logoutFlowRegistry")
    @Lazy(true)
    private FlowDefinitionRegistry logoutFlowRegistry;

    @Autowired
    @Qualifier("loginFlowRegistry")
    @Lazy(true)
    private FlowDefinitionRegistry loginFlowRegistry;
    
    @Autowired
    @Qualifier("webflowCipherExecutor")
    private CipherExecutor<byte[], byte[]> webflowCipherExecutor;
    
    @Value("${cas.webflow.keyalias:aes128}")
    private String keyAlias;

    @Autowired
    @Qualifier("authenticationThrottle")
    @Lazy(true)
    private HandlerInterceptor authenticationThrottle;

    @Value("${cas.themeResolver.pathprefix:/WEB-INF/view/jsp/}")
    private String pathPrefix;
    
    @Value("${cas.viewResolver.xmlFile:classpath:/META-INF/spring/views.xml}")
    private Resource xmlViewsFile;
    
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
     * View resolver resource bundle view resolver.
     *
     * @return the resource bundle view resolver
     */
    @Bean(name = "resourceBundleViewResolver")
    public ResourceBundleViewResolver resourceBundleViewResolver() {
        final ResourceBundleViewResolver resolver = new ResourceBundleViewResolver();
        resolver.setOrder(VIEW_RESOLVER_RESOURCE_ORDER);
        resolver.setBasename("cas_views");
        resolver.setCache(false);
        return resolver;
    }

    /**
     * Bean name view resolver bean name view resolver.
     *
     * @return the bean name view resolver
     */
    @Bean(name = "beanNameViewResolver")
    public BeanNameViewResolver beanNameViewResolver() {
        final BeanNameViewResolver bean = new BeanNameViewResolver();
        bean.setOrder(VIEW_RESOLVER_BEAN_ORDER);
        return bean;
    }

    /**
     * Xml view resolver abstract caching view resolver.
     *
     * @return the abstract caching view resolver
     */
    @Bean(name = "xmlViewResolver")
    public ViewResolver xmlViewResolver() {
        if (xmlViewsFile.exists()) {
            final XmlViewResolver bean = new XmlViewResolver();
            bean.setOrder(VIEW_RESOLVER_XML_ORDER);
            bean.setLocation(xmlViewsFile);
            return bean;
        }
        return beanNameViewResolver();
    }

    /**
     * Internal view resolver registered service theme based view resolver.
     *
     * @return the registered service theme based view resolver
     */
    @Bean(name = "registeredServiceThemeBasedViewResolver")
    public RegisteredServiceThemeBasedViewResolver registeredServiceThemeBasedViewResolver() {
        final RegisteredServiceThemeBasedViewResolver bean = new RegisteredServiceThemeBasedViewResolver(this.servicesManager);
        bean.setPrefix(this.pathPrefix);
        bean.setSuffix(".jsp");
        bean.setCache(false);
        bean.setOrder(VIEW_RESOLVER_SERVICE_ORDER);
        return bean;
    }
        
    /**
     * Url based view resolver url based view resolver.
     *
     * @return the url based view resolver
     */
    @Bean(name = "urlBasedViewResolver")
    public UrlBasedViewResolver urlBasedViewResolver() {
        final UrlBasedViewResolver bean = new UrlBasedViewResolver();
        bean.setViewClass(InternalResourceView.class);
        bean.setPrefix(this.pathPrefix);
        bean.setSuffix(".jsp");
        bean.setOrder(VIEW_RESOLVER_URL_ORDER);
        bean.setCache(false);
        return bean;
    }

    /**
     * Internal view resolver internal resource view resolver.
     *
     * @return the internal resource view resolver
     */
    @Bean(name = "internalViewResolver")
    public InternalResourceViewResolver internalViewResolver() {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix(this.resolverPathPrefix);
        resolver.setSuffix(".jsp");
        resolver.setOrder(VIEW_RESOLVER_INTERNAL_ORDER);
        resolver.setCache(false);
        return resolver;
    }
    
    /**
     * View factory creator mvc view factory creator.
     *
     * @return the mvc view factory creator
     */
    @Bean(name = "viewFactoryCreator")
    public MvcViewFactoryCreator viewFactoryCreator() {
        final MvcViewFactoryCreator resolver = new MvcViewFactoryCreator();
        resolver.setViewResolvers(ImmutableList.of(resourceBundleViewResolver(), beanNameViewResolver(),
                registeredServiceThemeBasedViewResolver(), internalViewResolver()));
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
    public CasDefaultFlowUrlHandler logoutFlowUrlHandler() {
        final CasDefaultFlowUrlHandler handler = new CasDefaultFlowUrlHandler();
        handler.setFlowExecutionKeyParameter("RelayState");
        return handler;
    }

    /**
     * Logout handler adapter selective flow handler adapter.
     *
     * @return the selective flow handler adapter
     */
    @Bean(name = "logoutHandlerAdapter")
    public SelectiveFlowHandlerAdapter logoutHandlerAdapter() {
        final SelectiveFlowHandlerAdapter handler = new SelectiveFlowHandlerAdapter();
        handler.setSupportedFlowId("logout");
        handler.setFlowExecutor(this.logoutFlowExecutor);
        handler.setFlowUrlHandler(logoutFlowUrlHandler());
        return handler;
    }
    
    /**
     * Login flow cipher bean buffered block cipher bean.
     *
     * @return the buffered block cipher bean
     */
    @Bean(name = "loginFlowCipherBean")
    public CipherBean loginFlowCipherBean() {

        try {
            return new CipherBean() {
                @Override
                public byte[] encrypt(final byte[] bytes) {
                    return webflowCipherExecutor.encode(bytes);
                }

                @Override
                public void encrypt(final InputStream inputStream, final OutputStream outputStream) {
                    throw new RuntimeException(new OperationNotSupportedException("Encrypting input stream is not supported"));
                }

                @Override
                public byte[] decrypt(final byte[] bytes) {
                    return webflowCipherExecutor.decode(bytes);
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
        handler.setFlowExecutor(this.loginFlowExecutor);
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
        handler.setFlowRegistry(logoutFlowRegistry);
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
        handler.setFlowRegistry(loginFlowRegistry);
        final Object[] interceptors = new Object[]{localeChangeInterceptor(), this.authenticationThrottle};
        handler.setInterceptors(interceptors);
        return handler;
    }
    
}

