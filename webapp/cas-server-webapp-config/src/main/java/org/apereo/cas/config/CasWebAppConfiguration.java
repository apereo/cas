package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.web.Log4jServletContextListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;

/**
 * This is {@link CasWebAppConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casWebAppConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasWebAppConfiguration implements WebMvcConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("localeChangeInterceptor")
    private LocaleChangeInterceptor localeChangeInterceptor;

    @RefreshScope
    @Bean
    @Lazy
    public ThemeChangeInterceptor themeChangeInterceptor() {
        val bean = new ThemeChangeInterceptor();
        bean.setParamName(casProperties.getTheme().getParamName());
        return bean;
    }

    @ConditionalOnMissingBean(name = "localeResolver")
    @Bean
    @Lazy
    public LocaleResolver localeResolver() {
        final CookieLocaleResolver bean = new CookieLocaleResolver() {
            @Override
            protected Locale determineDefaultLocale(final HttpServletRequest request) {
                val locale = request.getLocale();
                if (StringUtils.isBlank(casProperties.getLocale().getDefaultValue())
                    || !locale.getLanguage().equals(casProperties.getLocale().getDefaultValue())) {
                    return locale;
                }
                return new Locale(casProperties.getLocale().getDefaultValue());
            }
        };
        return bean;
    }

    @Bean
    @Lazy
    protected UrlFilenameViewController passThroughController() {
        return new UrlFilenameViewController();
    }

    @Bean
    protected Controller rootController() {
        return new ParameterizableViewController() {
            @Override
            protected ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                         final HttpServletResponse response) {
                val queryString = request.getQueryString();
                val url = request.getContextPath() + "/login"
                    + (queryString != null ? '?' + queryString : StringUtils.EMPTY);
                return new ModelAndView(new RedirectView(response.encodeURL(url)));
            }

        };
    }

    @Bean
    @Lazy
    public ServletListenerRegistrationBean log4jServletContextListener() {
        val bean = new ServletListenerRegistrationBean();
        bean.setEnabled(true);
        bean.setListener(new Log4jServletContextListener());
        return bean;
    }

    @Bean
    @Lazy
    public SimpleUrlHandlerMapping handlerMapping() {
        val mapping = new SimpleUrlHandlerMapping();

        val root = rootController();
        mapping.setOrder(1);
        mapping.setAlwaysUseFullPath(true);
        mapping.setRootHandler(root);
        val urls = new HashMap();
        urls.put("/", root);

        mapping.setUrlMap(urls);
        return mapping;
    }

    @Bean
    @Lazy
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor)
            .addPathPatterns("/**");
    }
}
