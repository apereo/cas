package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.RefreshableHandlerInterceptor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.CookieGenerator;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

/**
 * This is {@link CasWebAppConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebApplication)
@Configuration(value = "CasWebAppConfiguration", proxyBeanMethods = false)
class CasWebAppConfiguration {
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public ThemeChangeInterceptor themeChangeInterceptor(final CasConfigurationProperties casProperties) {
        val bean = new ThemeChangeInterceptor();
        bean.setParamName(casProperties.getTheme().getParamName());
        return bean;
    }

    @ConditionalOnMissingBean(name = "casLocaleResolver")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public LocaleResolver localeResolver(final CasConfigurationProperties casProperties) {
        val localeProps = casProperties.getLocale();
        val localeCookie = localeProps.getCookie();

        val resolver = new CookieLocaleResolver() {
            @Nonnull
            @Override
            protected Locale determineDefaultLocale(final HttpServletRequest request) {
                val locale = request.getLocale();
                if (StringUtils.isBlank(localeProps.getDefaultValue())
                    || !locale.getLanguage().equals(localeProps.getDefaultValue())) {
                    return locale;
                }
                return Locale.forLanguageTag(localeProps.getDefaultValue());
            }
        };
        resolver.setCookieDomain(localeCookie.getDomain());
        resolver.setCookiePath(StringUtils.defaultIfBlank(localeCookie.getPath(), CookieGenerator.DEFAULT_COOKIE_PATH));
        resolver.setCookieHttpOnly(localeCookie.isHttpOnly());
        resolver.setCookieSecure(localeCookie.isSecure());
        resolver.setCookieName(StringUtils.defaultIfBlank(localeCookie.getName(), CookieLocaleResolver.DEFAULT_COOKIE_NAME));
        resolver.setCookieMaxAge(localeCookie.getMaxAge());
        resolver.setLanguageTagCompliant(true);
        resolver.setRejectInvalidCookies(true);
        return resolver;
    }

    @Bean
    public SimpleUrlHandlerMapping handlerMapping(
        @Qualifier("rootController") final Controller rootController) {
        val mapping = new SimpleUrlHandlerMapping();

        mapping.setOrder(1);
        mapping.getUrlPathHelper().setAlwaysUseFullPath(true);
        mapping.setRootHandler(rootController);
        val urls = new HashMap<String, Object>();
        urls.put("/", rootController);

        mapping.setUrlMap(urls);
        return mapping;
    }

    @Bean
    public WebMvcConfigurer casWebAppWebMvcConfigurer(
        @Qualifier("localeChangeInterceptor")
        final ObjectProvider<HandlerInterceptor> localeChangeInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(
                @Nonnull final InterceptorRegistry registry) {
                registry.addInterceptor(new RefreshableHandlerInterceptor(localeChangeInterceptor)).addPathPatterns("/**");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "rootController")
    public Controller rootController() {
        return new ParameterizableViewController() {
            @Override
            protected ModelAndView handleRequestInternal(
                @Nonnull final HttpServletRequest request,
                @Nonnull final HttpServletResponse response) {
                val queryString = request.getQueryString();
                val url = request.getContextPath() + "/login"
                          + Optional.ofNullable(queryString).map(value -> '?' + value).orElse(StringUtils.EMPTY);
                return new ModelAndView(new RedirectView(response.encodeURL(url)));
            }

        };
    }

    @Bean
    protected UrlFilenameViewController passThroughController() {
        return new UrlFilenameViewController();
    }
}
