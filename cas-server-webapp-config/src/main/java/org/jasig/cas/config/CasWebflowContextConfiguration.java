package org.jasig.cas.config;

import com.google.common.collect.ImmutableList;
import org.cryptacular.bean.BufferedBlockCipherBean;
import org.cryptacular.bean.KeyStoreFactoryBean;
import org.cryptacular.generator.sp80038a.RBGNonce;
import org.cryptacular.io.URLResource;
import org.cryptacular.spec.BufferedBlockCipherSpec;
import org.jasig.cas.web.flow.LogoutConversionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.binding.convert.ConversionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.ResourceBundleViewResolver;
import org.springframework.webflow.config.FlowBuilderServicesBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.expression.spel.WebFlowSpringELExpressionParser;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;

/**
 * This is {@link CasWebflowContextConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casWebflowContextConfiguration")
public class CasWebflowContextConfiguration {

    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * The constant VIEW_RESOLVER_ORDER.
     */
    private static final int VIEW_RESOLVER_ORDER = 10000;

    /**
     * The Resolver path prefix.
     */
    @Value("${cas.themeResolver.pathprefix:/WEB-INF/view/jsp}/default/ui/")
    private String resolverPathPrefix;

    /**
     * The Keystore type.
     */
    @Value("${cas.webflow.keystore.type:JCEKS}")
    private String keystoreType;

    /**
     * The Keystore password.
     */
    @Value("${cas.webflow.keystore.password:changeit}")
    private String keystorePassword;

    /**
     * The Key password.
     */
    @Value("${cas.webflow.keypassword:changeit}")
    private String keyPassword;

    /**
     * The Keystore file.
     */
    @Value("${cas.webflow.keystore:classpath:/etc/keystore.jceks}")
    private Resource keystoreFile;

    /**
     * The Alg name.
     */
    @Value("${cas.webflow.cipher.alg:AES}")
    private String algName;

    /**
     * The Cipher mode.
     */
    @Value("${cas.webflow.cipher.mode:CBC}")
    private String cipherMode;

    /**
     * The Cipher padding.
     */
    @Value("${cas.webflow.cipher.padding:PKCS7}")
    private String cipherPadding;

    /**
     * The Key alias.
     */
    @Value("${cas.webflow.keyalias:aes128}")
    private String keyAlias;

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
        resolver.setOrder(VIEW_RESOLVER_ORDER);
        return resolver;
    }

    /**
     * View resolver resource bundle view resolver.
     *
     * @return the resource bundle view resolver
     */
    @Bean(name = "viewResolver")
    public ResourceBundleViewResolver viewResolver() {
        final ResourceBundleViewResolver resolver = new ResourceBundleViewResolver();
        resolver.setOrder(0);
        resolver.setBasename("cas_views");
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
        resolver.setViewResolvers(ImmutableList.of(viewResolver(), internalViewResolver()));
        return resolver;
    }

    /**
     * Login flow cipher bean buffered block cipher bean.
     *
     * @return the buffered block cipher bean
     */
    @Bean(name = "loginFlowCipherBean")
    public BufferedBlockCipherBean loginFlowCipherBean() {
        try {
            final KeyStoreFactoryBean factory = new KeyStoreFactoryBean(new URLResource(this.keystoreFile.getURL()),
                    this.keystoreType, this.keystorePassword);
            final BufferedBlockCipherBean resolver = new BufferedBlockCipherBean();
            resolver.setKeyAlias(this.keyAlias);
            resolver.setKeyStore(factory.newInstance());
            resolver.setKeyPassword(this.keyPassword);
            resolver.setNonce(new RBGNonce());
            resolver.setBlockCipherSpec(new BufferedBlockCipherSpec(this.algName, this.cipherMode, this.cipherPadding));
            return resolver;

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Bean(name="builder")
    public FlowBuilderServices builder() {
        final FlowBuilderServicesBuilder builder = new FlowBuilderServicesBuilder(this.applicationContext);
        builder.setViewFactoryCreator(viewFactoryCreator());
        builder.setExpressionParser(expressionParser());
        builder.setDevelopmentMode(true);
        return builder.build();
    }

}

