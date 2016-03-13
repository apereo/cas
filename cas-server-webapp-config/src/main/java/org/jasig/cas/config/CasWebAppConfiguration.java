package org.jasig.cas.config;

import org.apache.logging.log4j.web.Log4jServletContextListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.XmlViewResolver;

import javax.validation.MessageInterpolator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This is {@link CasWebAppConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casWebAppConfiguration")
@Lazy(true)
public class CasWebAppConfiguration extends WebMvcConfigurerAdapter {

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
     * The Xml views file.
     */
    @Value("${cas.viewResolver.xmlFile:classpath:/views.xml}")
    private Resource xmlViewsFile;

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
    public ViewResolver xmlViewResolver() {
        if (xmlViewsFile.exists()) {
            final XmlViewResolver bean = new XmlViewResolver();
            bean.setOrder(URL_VIEW_RESOLVER_ORDER - 1);
            bean.setLocation(xmlViewsFile);
            return bean;
        }
        return beanNameViewResolver();
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

    /**
     * Service theme resolver supported browsers map.
     *
     * @return the map
     */
    @Bean(name = "serviceThemeResolverSupportedBrowsers")
    public Map serviceThemeResolverSupportedBrowsers() {
        final Map<String, String> map = new HashMap<>();
        map.put(".*iPhone.*", "iphone");
        map.put(".*Android.*", "android");
        map.put(".*Safari.*Pre.*", "safari");
        map.put(".*iPhone.*", "iphone");
        map.put(".*Nokia.*AppleWebKit.*", "nokiawebkit");
        return map;
    }

    /**
     * Log4j servlet context listener servlet listener registration bean.
     *
     * @return the servlet listener registration bean
     */
    @Bean(name = "log4jServletContextListener")
    public ServletListenerRegistrationBean log4jServletContextListener() {
        final ServletListenerRegistrationBean bean = new ServletListenerRegistrationBean();
        bean.setEnabled(true);
        bean.setName("log4jServletContextListener");
        bean.setListener(new Log4jServletContextListener());
        return bean;
    }

    /**
     * Simple controller handler adapter simple controller handler adapter.
     *
     * @return the simple controller handler adapter
     */
    @Bean(name = "simpleControllerHandlerAdapter")
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }
}
