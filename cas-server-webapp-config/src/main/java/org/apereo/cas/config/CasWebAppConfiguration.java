package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.web.Log4jServletContextListener;
import org.apereo.cas.configuration.model.webapp.LocaleProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.MessageInterpolator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This is {@link CasWebAppConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casWebAppConfiguration")
@EnableConfigurationProperties(LocaleProperties.class)
public class CasWebAppConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    @Qualifier("messageInterpolator")
    private MessageInterpolator messageInterpolator;

    @Autowired
    LocaleProperties localeProperties;

    @Value("${cas.themeResolver.param.name:theme}")
    private String themeParamName;

    /**
     * Credentials validator local validator factory bean.
     *
     * @return the local validator factory bean
     */
    @RefreshScope
    @Bean
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
    @RefreshScope
    @Bean
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
    @Bean
    public CookieLocaleResolver localeResolver() {
        final CookieLocaleResolver bean = new CookieLocaleResolver() {
            @Override
            protected Locale determineDefaultLocale(final HttpServletRequest request) {
                final Locale locale = request.getLocale();
                if (StringUtils.isBlank(localeProperties.getDefaultValue())
                        || locale.getLanguage().equals(localeProperties.getDefaultValue())) {
                    return locale;
                }
                return new Locale(localeProperties.getDefaultValue());
            }
        };
        return bean;
    }

    /**
     * Locale change interceptor locale change interceptor.
     *
     * @return the locale change interceptor
     */
    @RefreshScope
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        final LocaleChangeInterceptor bean = new LocaleChangeInterceptor();
        bean.setParamName(this.localeProperties.getParamName());
        return bean;
    }

    /**
     * Service theme resolver supported browsers map.
     *
     * @return the map
     */
    @Bean
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
     * Root controller controller.
     *
     * @return the controller
     */
    @Bean
    protected Controller rootController() {
        return new ParameterizableViewController() {
            @Override
            protected ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                         final HttpServletResponse response)
                    throws Exception {
                final String queryString = request.getQueryString();
                final String url = request.getContextPath() + "/login" + (queryString != null ? '?' + queryString : "");
                return new ModelAndView(new RedirectView(response.encodeURL(url)));
            }

        };
    }

    /**
     * Log4j servlet context listener servlet listener registration bean.
     *
     * @return the servlet listener registration bean
     */
    @Bean
    public ServletListenerRegistrationBean log4jServletContextListener() {
        final ServletListenerRegistrationBean bean = new ServletListenerRegistrationBean();
        bean.setEnabled(true);
        bean.setName("log4jServletContextListener");
        bean.setListener(new Log4jServletContextListener());
        return bean;
    }

    /**
     * Handler mapping c simple url handler mapping.
     *
     * @return the simple url handler mapping
     */
    @Bean
    public SimpleUrlHandlerMapping handlerMapping() {
        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(1);
        mapping.setAlwaysUseFullPath(true);
        mapping.setRootHandler(rootController());
        return mapping;
    }

    /**
     * Simple controller handler adapter.
     *
     * @return the simple controller handler adapter
     */
    @Bean
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }
}
