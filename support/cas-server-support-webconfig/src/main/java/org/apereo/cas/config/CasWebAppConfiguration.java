package org.apereo.cas.config;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.RefreshableHandlerInterceptor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.ThemeChangeInterceptor;
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
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
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
    @ConditionalOnMissingBean(name = "casThemeChangeInterceptor")
    public HandlerInterceptor themeChangeInterceptor(
        @Qualifier("themeResolver") final ThemeResolver themeResolver,
        final CasConfigurationProperties casProperties) {
        return new ThemeChangeInterceptor(themeResolver, casProperties.getTheme().getParamName());
    }

    @Configuration(value = "CasWebAppLocaleConfiguration", proxyBeanMethods = false)
    static class CasWebAppLocaleConfiguration {
        @ConditionalOnMissingBean(name = "casLocaleResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LocaleResolver localeResolver(final CasConfigurationProperties casProperties) {
            val localeProps = casProperties.getLocale();
            val localeCookie = localeProps.getCookie();

            val cookieName = StringUtils.defaultIfBlank(localeCookie.getName(), CookieLocaleResolver.DEFAULT_COOKIE_NAME);
            val resolver = new CookieLocaleResolver(cookieName);
            resolver.setDefaultLocaleFunction(request -> {
                val locale = request.getLocale();
                if (StringUtils.isBlank(localeProps.getDefaultValue())
                    || !locale.getLanguage().equals(localeProps.getDefaultValue())) {
                    return locale;
                }
                return Locale.forLanguageTag(localeProps.getDefaultValue());
            });
            resolver.setCookieDomain(localeCookie.getDomain());
            resolver.setCookiePath(StringUtils.defaultIfBlank(localeCookie.getPath(), "/"));
            resolver.setCookieHttpOnly(localeCookie.isHttpOnly());
            resolver.setCookieSecure(localeCookie.isSecure());
            resolver.setCookieMaxAge(Duration.ofSeconds(CookieUtils.getCookieMaxAge(localeCookie.getMaxAge())));
            resolver.setLanguageTagCompliant(true);
            resolver.setRejectInvalidCookies(true);
            return resolver;
        }

        @Bean
        public WebMvcConfigurer casWebAppWebMvcConfigurer(
            @Qualifier("localeChangeInterceptor")
            final ObjectProvider<HandlerInterceptor> localeChangeInterceptor) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(@Nonnull final InterceptorRegistry registry) {
                    val interceptor = new RefreshableHandlerInterceptor(localeChangeInterceptor);
                    registry.addInterceptor(interceptor).addPathPatterns("/**");
                }
            };
        }
    }

    @Configuration(value = "CasWebAppControllersConfiguration", proxyBeanMethods = false)
    static class CasWebAppControllersConfiguration {

        @Bean
        public SimpleUrlHandlerMapping handlerMapping() {
            val mapping = new SimpleUrlHandlerMapping();
            mapping.setOrder(1);
            mapping.getUrlPathHelper().setAlwaysUseFullPath(true);
            val urls = new HashMap<String, Object>();
            val rootController = new RootController();
            mapping.setRootHandler(rootController);
            urls.put("/", rootController);
            mapping.setUrlMap(urls);
            return mapping;
        }

        @Bean
        public UrlFilenameViewController passThroughController() {
            return new UrlFilenameViewController();
        }

        private static final class RootController extends ParameterizableViewController {
            @Override
            protected ModelAndView handleRequestInternal(
                @Nonnull
                final HttpServletRequest request,
                @Nonnull
                final HttpServletResponse response) {
                val queryString = request.getQueryString();
                val url = request.getContextPath() + CasProtocolConstants.ENDPOINT_LOGIN
                    + Optional.ofNullable(queryString).map(value -> '?' + value).orElse(StringUtils.EMPTY);
                return new ModelAndView(new RedirectView(response.encodeURL(url)));
            }
        }
    }
}

