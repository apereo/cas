package org.jasig.cas.config;

import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.web.RegisteredServiceThemeBasedViewResolver;
import org.jasig.cas.web.flow.CasDefaultFlowUrlHandler;
import org.jasig.cas.web.flow.SelectiveFlowHandlerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.XmlViewResolver;
import org.springframework.web.servlet.view.script.ScriptTemplateViewResolver;
import org.springframework.webflow.executor.FlowExecutor;

import javax.validation.MessageInterpolator;
import java.util.Locale;

/**
 * This is {@link CasWebAppConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casWebAppConfiguration")
public class CasWebAppConfiguration {

    /**
     * The constant URL_VIEW_RESOLVER_ORDER.
     */
    private static final int URL_VIEW_RESOLVER_ORDER = 2000;

    /**
     * The Message interpolator.
     */
    @Autowired
    @Qualifier("messageInterpolator")
    private MessageInterpolator messageInterpolator;

    /**
     * The Theme param name.
     */
    @Value("${cas.themeResolver.param.name:theme}")
    private String themeParamName;

    /**
     * The Path prefix.
     */
    @Value("${cas.themeResolver.pathprefix:/WEB-INF/view/jsp}/")
    private String pathPrefix;


    /**
     * The Xml views file.
     */
    @Value("${cas.viewResolver.xmlFile:classpath:/META-INF/spring/views.xml}")
    private Resource xmlViewsFile;

    /**
     * The Services manager.
     */
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
    /**
     * The Default locale.
     */
    @Value("${locale.default:en}")
    private Locale defaultLocale;

    /**
     * The Locale param name.
     */
    @Value("${locale.param.name:locale}")
    private String localeParamName;

    /**
     * Credentials validator local validator factory bean.
     *
     * @return the local validator factory bean
     */
    @Bean(name = "credentialsValidator")
    public LocalValidatorFactoryBean credentialsValidator() {
        final LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setMessageInterpolator(this.messageInterpolator);
        return bean;
    }

    /**
     * Theme change interceptor theme change interceptor.
     *
     * @return the theme change interceptor
     */
    @Bean(name = "themeChangeInterceptor")
    public ThemeChangeInterceptor themeChangeInterceptor() {
        final ThemeChangeInterceptor bean = new ThemeChangeInterceptor();
        bean.setParamName(this.themeParamName);
        return bean;
    }

    /**
     * Bean name view resolver bean name view resolver.
     *
     * @return the bean name view resolver
     */
    @Bean(name = "beanNameViewResolver")
    public BeanNameViewResolver beanNameViewResolver() {
        final BeanNameViewResolver bean = new BeanNameViewResolver();
        bean.setOrder(1);
        return bean;
    }

    /**
     * Xml view resolver abstract caching view resolver.
     *
     * @return the abstract caching view resolver
     */
    @Bean(name = "xmlViewResolver")
    public AbstractCachingViewResolver xmlViewResolver() {
        if (xmlViewsFile.exists()) {
            final XmlViewResolver bean = new XmlViewResolver();
            bean.setOrder(URL_VIEW_RESOLVER_ORDER - 1);
            bean.setLocation(xmlViewsFile);
            return bean;
        }
        final ScriptTemplateViewResolver bean = new ScriptTemplateViewResolver();
        bean.setOrder(URL_VIEW_RESOLVER_ORDER - 1);
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
        bean.setOrder(URL_VIEW_RESOLVER_ORDER);
        return bean;
    }

    /**
     * Internal view resolver registered service theme based view resolver.
     *
     * @return the registered service theme based view resolver
     */
    @Bean(name = "internalViewResolver")
    public RegisteredServiceThemeBasedViewResolver internalViewResolver() {
        final RegisteredServiceThemeBasedViewResolver bean = new RegisteredServiceThemeBasedViewResolver(this.servicesManager);
        bean.setPrefix(this.pathPrefix);
        bean.setOrder(URL_VIEW_RESOLVER_ORDER + 1);
        return bean;
    }

    /**
     * Locale resolver cookie locale resolver.
     *
     * @return the cookie locale resolver
     */
    @Bean(name = "localeResolver")
    public CookieLocaleResolver localeResolver() {
        final CookieLocaleResolver bean = new CookieLocaleResolver();
        bean.setDefaultLocale(this.defaultLocale);
        return bean;
    }

    /**
     * Locale change interceptor locale change interceptor.
     *
     * @return the locale change interceptor
     */
    @Bean(name = "localeChangeInterceptor")
    public LocaleChangeInterceptor localeChangeInterceptor() {
        final LocaleChangeInterceptor bean = new LocaleChangeInterceptor();
        bean.setParamName(this.localeParamName);
        return bean;
    }


    @Bean(name = "simpleControllerHandlerAdapter")
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

}
