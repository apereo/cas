package org.apereo.cas.services.web.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.ChainingThemeResolver;
import org.apereo.cas.services.web.RegisteredServiceThemeResolver;
import org.apereo.cas.services.web.RequestHeaderThemeResolver;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.theme.CookieThemeResolver;
import org.springframework.web.servlet.theme.FixedThemeResolver;
import org.springframework.web.servlet.theme.SessionThemeResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link CasThemesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casThemesConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties({CasConfigurationProperties.class, ThymeleafProperties.class, WebProperties.class})
@Slf4j
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
public class CasThemesConfiguration {

    @Bean
    public Supplier<Map<String, String>> serviceThemeResolverSupportedBrowsers() {
        val map = new HashMap<String, String>();
        map.put(".*Android.*", "android");
        map.put(".*Safari.*Pre.*", "safari");
        map.put(".*iPhone.*", "iphone");
        map.put(".*Nokia.*AppleWebKit.*", "nokiawebkit");
        return () -> map;
    }

    @ConditionalOnMissingBean(name = "casThemeResolver")
    @Bean
    @Autowired
    public ThemeResolver themeResolver(
        @Qualifier("serviceThemeResolverSupportedBrowsers")
        final Supplier<Map<String, String>> serviceThemeResolverSupportedBrowsers,
        final CasConfigurationProperties casProperties,
        @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
        final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val defaultThemeName = casProperties.getTheme().getDefaultThemeName();
        val fixedResolver = new FixedThemeResolver();
        fixedResolver.setDefaultThemeName(defaultThemeName);
        val sessionThemeResolver = new SessionThemeResolver();
        sessionThemeResolver.setDefaultThemeName(defaultThemeName);
        val tgc = casProperties.getTgc();
        val cookieThemeResolver = new CookieThemeResolver();
        cookieThemeResolver.setDefaultThemeName(defaultThemeName);
        cookieThemeResolver.setCookieDomain(tgc.getDomain());
        cookieThemeResolver.setCookieHttpOnly(tgc.isHttpOnly());
        cookieThemeResolver.setCookieMaxAge(tgc.getMaxAge());
        cookieThemeResolver.setCookiePath(tgc.getPath());
        cookieThemeResolver.setCookieSecure(tgc.isSecure());
        val serviceThemeResolver = new RegisteredServiceThemeResolver(servicesManager,
            authenticationRequestServiceSelectionStrategies, casProperties,
            serviceThemeResolverSupportedBrowsers.get().entrySet().stream().collect(Collectors.toMap(entry -> Pattern.compile(entry.getKey()), Map.Entry::getValue)));
        serviceThemeResolver.setDefaultThemeName(defaultThemeName);
        val header = new RequestHeaderThemeResolver(casProperties.getTheme().getParamName());
        header.setDefaultThemeName(defaultThemeName);
        val chainingThemeResolver = new ChainingThemeResolver();
        chainingThemeResolver.addResolver(cookieThemeResolver)
            .addResolver(sessionThemeResolver).addResolver(header)
            .addResolver(serviceThemeResolver).addResolver(fixedResolver);
        chainingThemeResolver.setDefaultThemeName(defaultThemeName);
        return chainingThemeResolver;
    }

    @Bean
    @ConditionalOnMissingBean(name = "themesStaticResourcesWebMvcConfigurer")
    @Autowired
    public WebMvcConfigurer themesStaticResourcesWebMvcConfigurer(final CasConfigurationProperties casProperties,
                                                                  final WebProperties webProperties,
                                                                  final ThymeleafProperties thymeleafProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(final ResourceHandlerRegistry registry) {
                val templatePrefixes = casProperties.getView().getTemplatePrefixes();
                if (!templatePrefixes.isEmpty()) {
                    val registration = registry.addResourceHandler("/**");
                    val resources = templatePrefixes.stream().map(prefix -> StringUtils.appendIfMissing(prefix, "/"))
                            .map(Unchecked.function(ResourceUtils::getRawResourceFrom)).toArray(Resource[]::new);
                    LOGGER.debug("Adding resource handler for resources [{}]", (Object[]) resources);
                    registration.addResourceLocations(templatePrefixes.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
                    registration.addResourceLocations(webProperties.getResources().getStaticLocations());
                    FunctionUtils.doIfNotNull(webProperties.getResources().getCache().getPeriod(), period -> registration.setCachePeriod((int) period.getSeconds()));
                    registration.setCacheControl(webProperties.getResources().getCache().getCachecontrol().toHttpCacheControl());
                    registration.setUseLastModified(true);
                    val cache = thymeleafProperties != null && thymeleafProperties.isCache();
                    val chainRegistration = registration.resourceChain(cache);
                    val resolver = new PathResourceResolver();
                    resolver.setAllowedLocations(resources);
                    chainRegistration.addResolver(resolver);
                }
            }
        };
    }
}
