package org.jasig.cas.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;

import javax.validation.MessageInterpolator;
import java.util.Locale;

/**
 * This is {@link CasWebAppConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casWebAppConfiguration")
@Lazy(true)
public class CasWebAppConfiguration {

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
     * Simple controller handler adapter.
     *
     * @return the simple controller handler adapter
     */
    @Bean(name = "simpleControllerHandlerAdapter")
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

}
