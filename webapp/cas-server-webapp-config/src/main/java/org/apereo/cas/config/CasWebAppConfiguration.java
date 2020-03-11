package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

/**
 * This is {@link CasWebAppConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casWebAppConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasWebAppConfiguration implements WebMvcConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("localeChangeInterceptor")
    private ObjectProvider<LocaleChangeInterceptor> localeChangeInterceptor;

    @RefreshScope
    @Bean
    public ThemeChangeInterceptor themeChangeInterceptor() {
        val bean = new ThemeChangeInterceptor();
        bean.setParamName(casProperties.getTheme().getParamName());
        return bean;
    }

    @ConditionalOnMissingBean(name = "localeResolver")
    @Bean
    public LocaleResolver localeResolver() {
        val localeProps = casProperties.getLocale();
        val localeCookie = localeProps.getCookie();

        val resolver = new CookieLocaleResolver() {
            @Override
            protected Locale determineDefaultLocale(final HttpServletRequest request) {
                val locale = request.getLocale();
                if (StringUtils.isBlank(localeProps.getDefaultValue())
                    || !locale.getLanguage().equals(localeProps.getDefaultValue())) {
                    return locale;
                }
                return new Locale(localeProps.getDefaultValue());
            }
        };
        resolver.setCookieDomain(localeCookie.getDomain());
        resolver.setCookiePath(StringUtils.defaultIfBlank(localeCookie.getPath(), CookieLocaleResolver.DEFAULT_COOKIE_PATH));
        resolver.setCookieHttpOnly(localeCookie.isHttpOnly());
        resolver.setCookieSecure(localeCookie.isSecure());
        resolver.setCookieName(StringUtils.defaultIfBlank(localeCookie.getName(), CookieLocaleResolver.DEFAULT_COOKIE_NAME));
        resolver.setCookieMaxAge(localeCookie.getMaxAge());
        resolver.setLanguageTagCompliant(true);
        resolver.setRejectInvalidCookies(true);
        return resolver;
    }

    @Bean
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
                    + Optional.ofNullable(queryString).map(string -> '?' + string).orElse(StringUtils.EMPTY);
                return new ModelAndView(new RedirectView(response.encodeURL(url)));
            }

        };
    }

    @Bean
    public SimpleUrlHandlerMapping handlerMapping() {
        val mapping = new SimpleUrlHandlerMapping();

        val root = rootController();
        mapping.setOrder(1);
        mapping.setAlwaysUseFullPath(true);
        mapping.setRootHandler(root);
        val urls = new HashMap<String, Object>();
        urls.put("/", root);

        mapping.setUrlMap(urls);
        return mapping;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor.getObject())
            .addPathPatterns("/**");
    }
}
